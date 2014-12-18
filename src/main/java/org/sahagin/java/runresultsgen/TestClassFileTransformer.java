package org.sahagin.java.runresultsgen;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import org.openqa.selenium.io.IOUtils;
import org.sahagin.java.external.TestDoc;
import org.sahagin.java.external.adapter.AdapterContainer;
import org.sahagin.share.IllegalTestScriptException;
import org.sahagin.share.srctree.SrcTree;
import org.sahagin.share.srctree.TestFunction;
import org.sahagin.share.srctree.code.CodeLine;
import org.sahagin.share.yaml.YamlConvertException;

public class TestClassFileTransformer implements ClassFileTransformer {
    private String configFilePath;
    private SrcTree srcTree;

    public TestClassFileTransformer(String configFilePath, SrcTree srcTree)
            throws YamlConvertException, IllegalTestScriptException {
        this.configFilePath = configFilePath;
        this.srcTree = srcTree;
    }

    private String qualifiedName(CtMethod method) {
        return method.getDeclaringClass().getName() + "." + method.getName();
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
        if (AdapterContainer.globalInstance().isRootFunction(method)) {
            return false;
        }

        try {
            if (method.getAnnotation(TestDoc.class) != null) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        // TODO method overload is not supported
        String methodQualifiedName = qualifiedName(method);
        return AdapterContainer.globalInstance().getAdditionalTestDocs().getFuncTestDoc(
                methodQualifiedName) != null;
    }

    private List<CtMethod> allSubMethods(CtClass ctClass) {
        CtMethod[] allMethods = ctClass.getMethods();
        List<CtMethod> result = new ArrayList<CtMethod>(allMethods.length);
        for (CtMethod method : allMethods) {
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
            if (AdapterContainer.globalInstance().isRootFunction(method)) {
                result.add(method);
            }
        }
        return result;
    }

    private String hookInitializeSrc() {
        String hookClassName = RunResultGenerateHook.class.getCanonicalName();
        return String.format("%s.initialize(\"%s\");", hookClassName, configFilePath);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer)
                    throws IllegalClassFormatException {
        ClassPool classPool = ClassPool.getDefault();
        String hookClassName = RunResultGenerateHook.class.getCanonicalName();
        String initializeSrc = hookInitializeSrc();
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
            for (CtMethod subMethod : subMethods) {
                if (subMethod.isEmpty()) {
                    continue; // cannot hook empty method
                }
                int funcDeclaredStartLine;
                int funcDeclaredEndLine;
                try {
                    funcDeclaredStartLine = declareStartLine(subMethod);
                    funcDeclaredEndLine = declareEndLine(subMethod);
                } catch (RuntimeException e) {
                    // Maybe when this class is frozen.
                    // Since frozen classes are maybe system class, just ignore this exception
                    return null;
                }
                String subMethodName = qualifiedName(subMethod);
                TestFunction subFunction = StackLineUtils.getTestFunction(
                        srcTree, subMethodName, funcDeclaredStartLine, funcDeclaredEndLine);
                if (subFunction == null) {
                    continue;
                }
                for (CodeLine codeLine : subFunction.getCodeBody()) {
                    // insert hook to the next line of hook target line
                    // to take screen shot after the target line procedure has been finished
                    int actualInsertedLine = subMethod.insertAt(codeLine.getStartLine() + 1, false, null);
                    subMethod.insertAt(codeLine.getStartLine() + 1,
                            String.format("%s%s.beforeSubCodeBodyHook(\"%s\", %d, %d);",
                                    initializeSrc, hookClassName, subMethodName,
                                    codeLine.getStartLine(), actualInsertedLine));
                }
            }
            CtClass exceptionType = classPool.get(Throwable.class.getCanonicalName());
            List<CtMethod> rootMethods = allRootMethods(ctClass);
            for (CtMethod rootMethod : rootMethods) {
                if (rootMethod.isEmpty()) {
                    continue; // cannot hook empty method
                }
                TestFunction rootFunction = StackLineUtils.getTestFunction(
                        srcTree, qualifiedName(rootMethod),
                        declareStartLine(rootMethod), declareEndLine(rootMethod));
                if (rootFunction == null) {
                    continue;
                }
                String rootMethodName = qualifiedName(rootMethod);
                for (CodeLine codeLine : rootFunction.getCodeBody()) {
                    // insert hook to the next line of hook target line
                    // to take screen shot after the target line procedure has been finished
                    int actualInsertedLine = rootMethod.insertAt(codeLine.getStartLine() + 1, false, null);
                    rootMethod.insertAt(codeLine.getStartLine() + 1,
                            String.format("%s%s.beforeRootCodeBodyHook(\"%s\", %d, %d);",
                                    initializeSrc, hookClassName, rootMethodName,
                                    codeLine.getStartLine(), actualInsertedLine));
                }

                rootMethod.insertBefore(initializeSrc + hookClassName + ".beforeRootMethodHook();");
                rootMethod.addCatch(
                        "{ " + initializeSrc + hookClassName + ".rootMethodErrorHook($e); throw $e; }", exceptionType);
                rootMethod.insertAfter(initializeSrc + hookClassName + ".afterRootMethodHook();", true);

            }
            return ctClass.toBytecode();
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
