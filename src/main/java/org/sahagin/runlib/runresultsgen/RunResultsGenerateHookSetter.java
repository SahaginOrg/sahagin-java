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
        List<String> result = new ArrayList<String>(paramTypes.length);
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
        List<Pair<CtMethod, TestMethod>> result
        = new ArrayList<Pair<CtMethod, TestMethod>>(allMethods.length);
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
                for (int i = 0; i < subMethod.getCodeBody().size(); i++) {
                    CodeLine codeLine = subMethod.getCodeBody().get(i);
                    if (i + 1 < subMethod.getCodeBody().size()) {
                        CodeLine nextCodeLine = subMethod.getCodeBody().get(i + 1);
                        assert codeLine.getEndLine() <= nextCodeLine.getStartLine();
                        if (codeLine.getEndLine() == nextCodeLine.getStartLine()) {
                            // - if multiple statements exist on a line, insert hook only after the last statement
                            // - avoid insertion at the middle of the statement.
                            //   The problem happens when multi-line statements are like:
                            //   method(1);method(
                            //           2);
                            continue;
                        }
                    }

                    // Hook should be inserted just after the code has finished
                    // since the code inserted by the insertAt method is inserted just before the specified line.
                    int insertedLine = codeLine.getEndLine() + 1;
                    int actualInsertedLine = ctSubMethod.insertAt(insertedLine, false, null);
                    ctSubMethod.insertAt(insertedLine,
                            String.format("%s%s.beforeCodeLineHook(\"%s\",\"%s\",\"%s\",\"%s\",%d, %d);",
                                    initializeSrc, hookClassName, subClassQualifiedName,
                                    subMethodSimpleName, subMethodSimpleName,
                                    subMethodArgClassesStr, codeLine.getStartLine(), actualInsertedLine));
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
                for (int i = 0; i < rootMethod.getCodeBody().size(); i++) {
                    CodeLine codeLine = rootMethod.getCodeBody().get(i);
                    if (i + 1 < rootMethod.getCodeBody().size()) {
                        CodeLine nextCodeLine = rootMethod.getCodeBody().get(i + 1);
                        assert codeLine.getEndLine() <= nextCodeLine.getStartLine();
                        if (codeLine.getEndLine() == nextCodeLine.getStartLine()) {
                            // - if multiple statements exist on a line, insert hook only after the last statement
                            // - avoid insertion at the middle of the statement.
                            //   The problem happens when multi-line statements are like:
                            //   method(1);method(
                            //           2);
                            continue;
                            // TODO screen capture is not taken correctly for multiple statements in a line
                        }
                    }

                    // Hook should be inserted just after the code has finished
                    // since the code inserted by the insertAt method is inserted just before the specified line.
                    int insertedLine = codeLine.getEndLine() + 1;
                    int actualInsertedLine = ctRootMethod.insertAt(insertedLine, false, null);
                    ctRootMethod.insertAt(insertedLine,
                            String.format("%s%s.beforeCodeLineHook(\"%s\",\"%s\",\"%s\",\"%s\",%d,%d);",
                                    initializeSrc, hookClassName, rootClassQualifiedName,
                                    rootMethodSimpleName, rootMethodSimpleName,
                                    rootMethodArgClassesStr, codeLine.getStartLine(), actualInsertedLine));
                }

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
