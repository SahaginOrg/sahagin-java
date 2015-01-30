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

import org.openqa.selenium.io.IOUtils;
import org.sahagin.runlib.external.TestDoc;
import org.sahagin.runlib.external.adapter.AdapterContainer;
import org.sahagin.share.IllegalTestScriptException;
import org.sahagin.share.Logging;
import org.sahagin.share.srctree.SrcTree;
import org.sahagin.share.srctree.TestMethod;
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

    private int declareStartLine(CtMethod method) {
        return method.getMethodInfo().getLineNumber(0);
    }

    private int declareEndLine(CtMethod method) {
        return method.getMethodInfo().getLineNumber(
                method.getMethodInfo().getCodeAttribute().getCodeLength());
    }

    private boolean isSubMethod(CtMethod method) {
        // root method also may be TestDoc method
        if (AdapterContainer.globalInstance().isRootMethod(method)) {
            return false;
        }

        try {
            if (method.getAnnotation(TestDoc.class) != null) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        String classQualifiedName = method.getDeclaringClass().getName();
        String methodSimpleName = method.getName();
        CtClass[] paramTypes;
        try {
            paramTypes = method.getParameterTypes();
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
        List<String> argClassQualifiedNames = new ArrayList<String>(paramTypes.length);
        for (CtClass paramType : paramTypes) {
            argClassQualifiedNames.add(paramType.getName());
        }
        return AdapterContainer.globalInstance().getAdditionalTestDocs().getMethodTestDoc(
                classQualifiedName, methodSimpleName, argClassQualifiedNames) != null;
    }

    private List<CtMethod> allSubMethods(CtClass ctClass) {
        CtMethod[] allMethods = ctClass.getMethods();
        List<CtMethod> result = new ArrayList<CtMethod>(allMethods.length);
        for (CtMethod method : allMethods) {
            if (!method.getDeclaringClass().getName().equals(ctClass.getName())) {
                // methods defined on superclass are also included in the result list of
                // CtClass.getMethods, so exclude such methods
                continue;
            }
            if (isSubMethod(method)) {
                result.add(method);
            }
        }
        return result;
    }

    private List<CtMethod> allRootMethods(CtClass ctClass) {
        CtMethod[] allMethods = ctClass.getMethods();
        List<CtMethod> result = new ArrayList<CtMethod>(allMethods.length);
        for (CtMethod method : allMethods) {
            if (!method.getDeclaringClass().getName().equals(ctClass.getName())) {
                // methods defined on superclass are also included in the result list of
                // CtClass.getMethods, so exclude such methods
                continue;
            }
            if (AdapterContainer.globalInstance().isRootMethod(method)) {
                result.add(method);
            }
        }
        return result;
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
        // - To avoid unexpected behaviours.
        //   For example, if transforms java.lang.invoke.** classes in Java8
        //   even without any ctClass modification, the classes become broken
        //   and Java stream API call fails unexpectedly.
        //   Maybe this is because CtClass instance generated by ClassPool.makeClass method
        //   is cached on global default ClassPool instance.
        if (isJavaSystemClassName(className)) {
            return null;
        }

        // TODO don't need to do anything for java package classes
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
            List<CtMethod> subMethods = allSubMethods(ctClass);
            for (CtMethod ctSubMethod : subMethods) {
                if (ctSubMethod.isEmpty()) {
                    continue; // cannot hook empty method
                }
                int methodDeclaredStartLine;
                int methodDeclaredEndLine;
                try {
                    methodDeclaredStartLine = declareStartLine(ctSubMethod);
                    methodDeclaredEndLine = declareEndLine(ctSubMethod);
                } catch (RuntimeException e) {
                    // Maybe when this class is frozen.
                    // Since frozen classes are maybe system class, just ignore this exception
                    logger.log(Level.INFO, "", e);
                    return null;
                }

                String subClassQualifiedName = ctSubMethod.getDeclaringClass().getName();
                String subMethodSimpleName = ctSubMethod.getName();
                TestMethod subMethod = StackLineUtils.getTestMethod(
                        srcTree, subClassQualifiedName, subMethodSimpleName,
                        methodDeclaredStartLine, methodDeclaredEndLine);
                if (subMethod == null) {
                    continue;
                }
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

                    // insert the hook code to the next line of the hook target line
                    // to take screen shot after the target line procedure has been finished
                    // (the code inserted by the insertAt method is inserted just before the target line)
                    int actualInsertedLine = ctSubMethod.insertAt(codeLine.getEndLine() + 1, false, null);
                    ctSubMethod.insertAt(codeLine.getEndLine() + 1,
                            String.format("%s%s.beforeSubCodeBodyHook(\"%s\", \"%s\", %d, %d);",
                                    initializeSrc, hookClassName, subClassQualifiedName, subMethodSimpleName,
                                    codeLine.getStartLine(), actualInsertedLine));
                    transformed = true;
                }
            }
            CtClass exceptionType = classPool.get(Throwable.class.getCanonicalName());
            List<CtMethod> rootMethods = allRootMethods(ctClass);
            for (CtMethod ctRootMethod : rootMethods) {
                if (ctRootMethod.isEmpty()) {
                    continue; // cannot hook empty method
                }
                String rootClassQualifiedName = ctRootMethod.getDeclaringClass().getName();
                String rootMethodSimpleName = ctRootMethod.getName();
                TestMethod rootMethod = StackLineUtils.getTestMethod(
                        srcTree, rootClassQualifiedName, rootMethodSimpleName,
                        declareStartLine(ctRootMethod), declareEndLine(ctRootMethod));
                if (rootMethod == null) {
                    continue;
                }
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

                    // insert the hook code to the next line of the hook target line
                    // to take screen shot after the target line procedure has been finished
                    // (the code inserted by the insertAt method is inserted just before the target line)
                    int actualInsertedLine = ctRootMethod.insertAt(codeLine.getEndLine() + 1, false, null);
                    ctRootMethod.insertAt(codeLine.getEndLine() + 1,
                            String.format("%s%s.beforeRootCodeBodyHook(\"%s\", \"%s\", %d, %d);",
                                    initializeSrc, hookClassName, rootClassQualifiedName, rootMethodSimpleName,
                                    codeLine.getStartLine(), actualInsertedLine));
                }

                ctRootMethod.insertBefore(initializeSrc + hookClassName + ".beforeRootMethodHook();");
                ctRootMethod.addCatch(
                        "{ " + initializeSrc + hookClassName + ".rootMethodErrorHook($e); throw $e; }", exceptionType);
                ctRootMethod.insertAfter(initializeSrc + hookClassName + ".afterRootMethodHook();", true);
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
