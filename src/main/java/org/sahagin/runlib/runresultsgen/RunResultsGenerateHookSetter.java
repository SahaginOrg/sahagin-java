package org.sahagin.runlib.runresultsgen;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.io.IOUtils;
import org.sahagin.share.IllegalTestScriptException;
import org.sahagin.share.Logging;
import org.sahagin.share.srctree.SrcTree;
import org.sahagin.share.srctree.TestMethod;
import org.sahagin.share.srctree.TestMethodTable;
import org.sahagin.share.srctree.code.CodeLine;
import org.sahagin.share.yaml.YamlConvertException;

public class RunResultsGenerateHookSetter implements ClassFileTransformer {
    private static Logger logger = Logging.getLogger(RunResultsGenerateHookSetter.class.getName());
    private String configFilePath;
    private SrcTree srcTree;

    public RunResultsGenerateHookSetter(String configFilePath, SrcTree srcTree)
            throws YamlConvertException, IllegalTestScriptException {
        this.configFilePath = configFilePath;
        this.srcTree = srcTree;
    }

    // throws NotFoundException if fails to get names
    // (for example when class of method argument type has not been loaded by class loader)
    private List<String> getArgClassQualifiedNames(CtMethod method) throws NotFoundException {
        CtClass[] paramTypes = method.getParameterTypes();
        List<String> result = new ArrayList<>(paramTypes.length);
        for (CtClass paramType : paramTypes) {
            result.add(paramType.getName());
        }
        return result;
    }

    // throws NotFoundException if fails to get names
    // (for example when class of method argument type has not been loaded by class loader)
    private String generateMethodKey(CtMethod method) throws NotFoundException {
        String classQualifiedName = method.getDeclaringClass().getName();
        String methodSimpleName = method.getName();
        List<String> argClassQualifiedNames = getArgClassQualifiedNames(method);
        return TestMethod.generateMethodKey(classQualifiedName, methodSimpleName, argClassQualifiedNames);
    }

    private List<Pair<CtMethod, TestMethod>> allMethodsSub(
            TestMethodTable table, CtClass ctClass) {
        CtMethod[] allMethods = ctClass.getMethods();
        List<Pair<CtMethod, TestMethod>> result = new ArrayList<>(allMethods.length);
        for (CtMethod ctMethod : allMethods) {
            if (!ctMethod.getDeclaringClass().getName().equals(ctClass.getName())) {
                // methods defined on superclass are also included in the result list of
                // CtClass.getMethods, so exclude such methods
                continue;
            }
            try {
                TestMethod testMethod = table.getByKey(generateMethodKey(ctMethod));
                if (testMethod != null) {
                    result.add(Pair.of(ctMethod, testMethod));
                }
            } catch (NotFoundException e) {
                // just ignore this method
                logger.log(Level.INFO, "ignore exceptin for " + ctMethod.getLongName(), e);
            }
        }
        return result;
    }

    private List<Pair<CtMethod, TestMethod>> allRootMethods(SrcTree srcTree, CtClass ctClass) {
        return allMethodsSub(srcTree.getRootMethodTable(), ctClass);
    }

    private List<Pair<CtMethod, TestMethod>> allSubMethods(SrcTree srcTree, CtClass ctClass) {
        return allMethodsSub(srcTree.getSubMethodTable(), ctClass);
    }

    private String hookInitializeSrc() {
        String hookClassName = HookMethodDef.class.getCanonicalName();
        return String.format("%s.initialize(\"%s\");", hookClassName, configFilePath);
    }

    // TODO should return false if user should define class java/.. or javax/...
    private boolean isJavaSystemClassName(String className) {
        if (className == null) {
            return false;
        }

        if (className.startsWith("java/") || className.startsWith("javax/")) {
            return true;
        }
        return false;
    }

    // Returns true if the statement for the codeLineIndex is the last statement for the containing line.
    // This may return false since multiple statement can be found in a line.
    private boolean isLineLastStament(TestMethod method, int codeLineIndex) {
        CodeLine codeLine = method.getCodeBody().get(codeLineIndex);
        if (codeLineIndex == method.getCodeBody().size() - 1) {
            return true;
        }

        CodeLine nextCodeLine = method.getCodeBody().get(codeLineIndex + 1);
        assert codeLine.getEndLine() <= nextCodeLine.getStartLine();
        if (codeLine.getEndLine() == nextCodeLine.getStartLine()) {
            // if next statement exists in the same line,
            // this statement is not the last statement for the line
            return false;
        }
        return true;
    }

    // beforeHook insertion target line for the specified codeLineIndex.
    // Returns -1 if beforeHook for the codeLineIndex should not be inserted
    private int beforeHookInsertLine(TestMethod method, int codeLineIndex) {
        if (!isLineLastStament(method, codeLineIndex)) {
            // don't insert the beforeHook since afterHook does not inserted to this line
            return -1;
        }

        // so that not to insert the hook to the middle of the line,
        // search the line top statement and insert hook to the statement line
        for (int i = codeLineIndex; i > 0; i--) {
            CodeLine thisLine = method.getCodeBody().get(i);
            CodeLine prevLine = method.getCodeBody().get(i - 1);
            assert prevLine.getEndLine() <= thisLine.getEndLine();
            if (prevLine.getEndLine() != thisLine.getStartLine()) {
                return thisLine.getStartLine();
            }
        }
        return method.getCodeBody().get(0).getStartLine();
    }

    // afterHook insertion target line for the specified codeLineIndex.
    // Returns -1 if afterHook for the codeLineIndex should not be inserted
    private int afterHookInsertLine(TestMethod method, int codeLineIndex) {
        // if multiple statements exist in one line, afterHook is inserted only after the last statement,
        // since when multi-line statements are like:
        // method(1);method(
        //         2);
        // insertion to the middle of the statement causes problem
        if (!isLineLastStament(method, codeLineIndex)) {
            return -1;
        }
        // insert hook to the next line of the codeLine
        // since insertAt method inserts code just before the specified line
        CodeLine codeLine = method.getCodeBody().get(codeLineIndex);
        return codeLine.getEndLine() + 1;
    }

    // - set beforeCodeLineHook and afterCodeLineHook for the each CodeLine of the specified method body
    // - returns true this method actually transform ctMethod body
    private boolean insertCodeBodyHook(TestMethod method, CtMethod ctMethod,
            String classQualifiedName, String methodSimpleName, String methodArgClassesStr) throws CannotCompileException {
        String hookClassName = HookMethodDef.class.getCanonicalName();
        String initializeSrc = hookInitializeSrc();
        boolean transformed = false;

        // iterate code body in the inverse order,
        // so that beforeHook is always inserted after the afterHook of the previous line
        // even if target line of these two hooks are the same
        for (int i = method.getCodeBody().size() - 1; i >= 0; i--) {
            int hookedLine = method.getCodeBody().get(i).getStartLine();

            // insert afterHook first and beforeHook second in each iteration,
            // so that beforeHook is always inserted before the afterHook
            // even if actual inserted lines for these two hooks are the same

            int afterHookInsertedLine = afterHookInsertLine(method, i);
            if (afterHookInsertedLine != -1) {
                int actualAfterHookInsertedLine = ctMethod.insertAt(afterHookInsertedLine, false, null);
                ctMethod.insertAt(afterHookInsertedLine,
                        String.format("%s%s.afterCodeLineHook(\"%s\",\"%s\",\"%s\",\"%s\",%d, %d);",
                                initializeSrc, hookClassName, classQualifiedName,
                                methodSimpleName, methodSimpleName,
                                methodArgClassesStr, hookedLine, actualAfterHookInsertedLine));
                transformed = true;
            }

            int beforeHookInsertedLine = beforeHookInsertLine(method, i);
            if (beforeHookInsertedLine != -1) {
                int actualBeforeHookInsertedLine = ctMethod.insertAt(beforeHookInsertedLine, false, null);
                ctMethod.insertAt(beforeHookInsertedLine,
                        String.format("%s%s.beforeCodeLineHook(\"%s\",\"%s\",\"%s\",\"%s\",%d, %d);",
                                initializeSrc, hookClassName, classQualifiedName,
                                methodSimpleName, methodSimpleName,
                                methodArgClassesStr, hookedLine, actualBeforeHookInsertedLine));
                transformed = true;
            }
        }

        return transformed;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer)
                    throws IllegalClassFormatException {
        // Don't transform system classes. This reason is:
        // - To improve performance
        // - To avoid unexpected behaviors.
        //   For example, if transforms java.lang.invoke.** classes in Java8
        //   even without any ctClass modification, the classes become broken
        //   and Java stream API call fails unexpectedly.
        //   Maybe this is because CtClass instance generated by ClassPool.makeClass method
        //   is cached on global default ClassPool instance.
        if (isJavaSystemClassName(className)) {
            return null;
        }

        ClassPool classPool = ClassPool.getDefault();
        String hookClassName = HookMethodDef.class.getCanonicalName();
        String initializeSrc = hookInitializeSrc();
        boolean transformed = false;
        InputStream stream = null;
        try {
            stream = new ByteArrayInputStream(classfileBuffer);
            CtClass ctClass = null;
            try {
                ctClass = classPool.makeClass(stream, true);
            } catch (RuntimeException e) {
                // makeClass raises RuntimeException when the existing class is frozen.
                // Since frozen classes are maybe system class, just ignore this exception
                return null;
            }

            for (Pair<CtMethod, TestMethod> pair : allSubMethods(srcTree, ctClass)) {
                CtMethod ctSubMethod = pair.getLeft();
                TestMethod subMethod = pair.getRight();
                if (ctSubMethod.isEmpty()) {
                    logger.info("skip empty method: " + ctSubMethod.getLongName());
                    continue; // cannot hook empty method
                }

                String subClassQualifiedName = subMethod.getTestClass().getQualifiedName();
                String subMethodSimpleName = subMethod.getSimpleName();
                String subMethodArgClassesStr = TestMethod.argClassQualifiedNamesToArgClassesStr(
                        getArgClassQualifiedNames(ctSubMethod));
                boolean insertResult = insertCodeBodyHook(
                        subMethod, ctSubMethod, subClassQualifiedName, subMethodSimpleName, subMethodArgClassesStr);
                if (insertResult) {
                    transformed = true;
                }
            }

            CtClass exceptionType = classPool.get(Throwable.class.getCanonicalName());
            for (Pair<CtMethod, TestMethod> pair : allRootMethods(srcTree, ctClass)) {
                CtMethod ctRootMethod = pair.getLeft();
                TestMethod rootMethod = pair.getRight();
                if (ctRootMethod.isEmpty()) {
                    continue; // cannot hook empty method
                }

                String rootClassQualifiedName = rootMethod.getTestClass().getQualifiedName();
                String rootMethodSimpleName = rootMethod.getSimpleName();
                String rootMethodArgClassesStr = TestMethod.argClassQualifiedNamesToArgClassesStr(
                        getArgClassQualifiedNames(ctRootMethod));
                insertCodeBodyHook(rootMethod, ctRootMethod, rootClassQualifiedName, rootMethodSimpleName, rootMethodArgClassesStr);
                ctRootMethod.insertBefore(String.format("%s%s.beforeMethodHook(\"%s\",\"%s\",\"%s\");",
                        initializeSrc, hookClassName, rootClassQualifiedName, rootMethodSimpleName, rootMethodSimpleName));
                ctRootMethod.addCatch(String.format("{ %s%s.methodErrorHook(\"%s\",\"%s\",$e); throw $e; }",
                        initializeSrc, hookClassName, rootClassQualifiedName, rootMethodSimpleName), exceptionType);
                ctRootMethod.insertAfter(String.format("%s%s.afterMethodHook(\"%s\",\"%s\");",
                        initializeSrc, hookClassName, rootClassQualifiedName, rootMethodSimpleName), true);
                transformed = true;
            }

            // don't transform not changed ctClass
            // (to improve performance and avoid unexpected error)
            if (transformed) {
                logger.info("transform " + className);
                return ctClass.toBytecode();
            } else {
                return null;
            }
        } catch (CannotCompileException e) {
            // print error since exception in transform method is just ignored
            System.err.println("exception on " + className);
            e.printStackTrace();
            throw new IllegalClassFormatException(e.getLocalizedMessage());
        } catch (Exception e) {
            // print error since exception in transform method is just ignored
            System.err.println("exception on " + className);
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }
}
