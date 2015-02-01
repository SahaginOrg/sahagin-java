package org.sahagin.runlib.srctreegen;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.jar.Manifest;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.sahagin.runlib.additionaltestdoc.AdditionalClassTestDoc;
import org.sahagin.runlib.additionaltestdoc.AdditionalMethodTestDoc;
import org.sahagin.runlib.additionaltestdoc.AdditionalPage;
import org.sahagin.runlib.additionaltestdoc.AdditionalTestDocs;
import org.sahagin.runlib.external.CaptureStyle;
import org.sahagin.runlib.external.adapter.AdapterContainer;
import org.sahagin.share.AcceptableLocales;
import org.sahagin.share.CommonUtils;
import org.sahagin.share.IllegalTestScriptException;
import org.sahagin.share.Logging;
import org.sahagin.share.srctree.PageClass;
import org.sahagin.share.srctree.SrcTree;
import org.sahagin.share.srctree.TestClass;
import org.sahagin.share.srctree.TestClassTable;
import org.sahagin.share.srctree.TestMethod;
import org.sahagin.share.srctree.TestMethodTable;
import org.sahagin.share.srctree.code.Code;
import org.sahagin.share.srctree.code.CodeLine;
import org.sahagin.share.srctree.code.MethodArgument;
import org.sahagin.share.srctree.code.StringCode;
import org.sahagin.share.srctree.code.SubMethodInvoke;
import org.sahagin.share.srctree.code.UnknownCode;

public class SrcTreeGenerator {
    private static Logger logger = Logging.getLogger(SrcTreeGenerator.class.getName());
    private AdditionalTestDocs additionalTestDocs;
    private AcceptableLocales locales;

    // additionalTestDocs can be null
    public SrcTreeGenerator(AdditionalTestDocs additionalTestDocs, AcceptableLocales locales) {
        if (additionalTestDocs == null) {
            // use empty additionalTestDocs
            this.additionalTestDocs = new AdditionalTestDocs();
        } else {
            this.additionalTestDocs = additionalTestDocs;
        }
        this.locales = locales;
    }

    // result first value .. TestDoc value. return null if no TestDoc found
    // result second value.. isPage
    private Pair<String, Boolean> getTestDoc(ITypeBinding type) {
        // Page testDoc is prior to TestDoc value
        String pageTestDoc = ASTUtils.getPageTestDoc(type, locales);
        if (pageTestDoc != null) {
            return Pair.of(pageTestDoc, true);
        }

        String testDoc = ASTUtils.getTestDoc(type, locales);
        if (testDoc != null) {
            return Pair.of(testDoc, false);
        }
        AdditionalClassTestDoc additional
        = additionalTestDocs.getClassTestDoc(type.getQualifiedName());
        if (additional != null) {
            return Pair.of(additional.getTestDoc(), additional instanceof AdditionalPage);
        }
        return Pair.of(null, false);
    }

    // null pair if not found
    private Pair<String, CaptureStyle> getTestDoc(IMethodBinding method) {
        Pair<String, CaptureStyle> pair = ASTUtils.getTestDoc(method, locales);
        if (pair.getLeft() != null) {
            return pair;
        }

        ITypeBinding[] paramTypes = method.getParameterTypes();
        List<String> argClassQualifiedNames = new ArrayList<String>(paramTypes.length);
        for (ITypeBinding param : paramTypes) {
            argClassQualifiedNames.add(param.getQualifiedName());
        }
        AdditionalMethodTestDoc additional = additionalTestDocs.getMethodTestDoc(
                method.getDeclaringClass().getQualifiedName(), method.getName(), argClassQualifiedNames);
        if (additional != null) {
            return Pair.of(additional.getTestDoc(), additional.getCaptureStyle());
        }
        return Pair.of(null, null);
    }

    private boolean isSubMethod(IMethodBinding methodBinding) {
        // rootMethod also can have its TestDoc value
        if (AdapterContainer.globalInstance().isRootMethod(methodBinding)) {
            return false;
        }
        return getTestDoc(methodBinding).getLeft() != null;
    }

    // srcFiles..parse target files
    // classPathEntries.. all class paths (class file containing directory or jar file) srcFiles depend
    private static void parseAST(
            String[] srcFiles, String srcEncoding, String[] classPathEntries, FileASTRequestor requestor) {
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        Map<?, ?> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
        parser.setCompilerOptions(options);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);
        parser.setEnvironment(classPathEntries, null, null, true);
        String[] srcEncodings = new String[srcFiles.length];
        for (int i = 0; i < srcEncodings.length; i++) {
            srcEncodings[i] = srcEncoding;
        }
        parser.createASTs(
                srcFiles, srcEncodings, new String[]{}, requestor, null);
    }

    // Get all root methods and its class as TestRootClass.
    // Each TestRootClass owns only TestRootMethod, and TestSubMethod information is not set yet.
    // methods does not have code information yet.
    private class CollectRootVisitor extends ASTVisitor {
        private TestClassTable rootClassTable;
        private TestMethodTable rootMethodTable;

        // old value in rootClassTable is not replaced.
        // old value in rootMethodTable is replaced.
        public CollectRootVisitor(
                TestClassTable rootClassTable, TestMethodTable rootMethodTable) {
            this.rootClassTable = rootClassTable;
            this.rootMethodTable = rootMethodTable;
        }

        @Override
        public boolean visit(MethodDeclaration node) {
            IMethodBinding methodBinding = node.resolveBinding();
            if (!AdapterContainer.globalInstance().isRootMethod(methodBinding)) {
                return super.visit(node);
            }

            ITypeBinding classBinding = methodBinding.getDeclaringClass();
            if (!classBinding.isClass() && !classBinding.isInterface()) {
                // enum method, etc
                return super.visit(node);
            }

            TestClass rootClass = rootClassTable.getByKey(classBinding.getQualifiedName());
            if (rootClass == null) {
                Pair<String, Boolean> pair = getTestDoc(classBinding);
                if (pair.getRight()) {
                    rootClass = new PageClass(); // though root class cannot be page class
                } else {
                    rootClass = new TestClass();
                }
                rootClass.setKey(classBinding.getQualifiedName());
                rootClass.setQualifiedName(classBinding.getQualifiedName());
                rootClass.setTestDoc(pair.getLeft());
                rootClassTable.addTestClass(rootClass);
            }

            TestMethod testMethod = new TestMethod();
            testMethod.setKey(methodBinding.getKey());
            testMethod.setSimpleName(methodBinding.getName());
            Pair<String, CaptureStyle> pair = getTestDoc(methodBinding);
            if (pair.getLeft() != null) {
                // pair is null if the root method does not have TestDoc annotation
                testMethod.setTestDoc(pair.getLeft());
                testMethod.setCaptureStyle(pair.getRight());
            }
            for (Object element : node.parameters()) {
                if (!(element instanceof SingleVariableDeclaration)) {
                    throw new RuntimeException("not supported yet: " + element);
                }
                SingleVariableDeclaration varDecl = (SingleVariableDeclaration)element;
                testMethod.addArgVariable(varDecl.getName().getIdentifier());
                if (varDecl.isVarargs()) {
                    testMethod.setVariableLengthArgIndex(testMethod.getArgVariables().size() - 1);
                }
            }
            testMethod.setTestClassKey(rootClass.getKey());
            testMethod.setTestClass(rootClass);
            rootMethodTable.addTestMethod(testMethod);

            rootClass.addTestMethodKey(testMethod.getKey());
            rootClass.addTestMethod(testMethod);

            return super.visit(node);
        }
    }

    private class CollectRootRequestor extends FileASTRequestor {
        private TestClassTable rootClassTable;
        private TestMethodTable rootMethodTable;

        public CollectRootRequestor() {
            rootClassTable = new TestClassTable();
            rootMethodTable = new TestMethodTable();
        }

        public TestClassTable getRootClassTable() {
            return rootClassTable;
        }

        public TestMethodTable getRootMethodTable() {
            return rootMethodTable;
        }

        @Override
        public void acceptAST(String sourceFilePath, CompilationUnit ast) {
            ast.accept(new CollectRootVisitor(rootClassTable, rootMethodTable));
        }
    }

    private class CollectSubVisitor extends ASTVisitor {
        private TestClassTable subClassTable;
        private TestMethodTable subMethodTable;
        private TestClassTable rootClassTable;

        // rootClassTable if only for read, not write any data.
        // old value in subClassTable is not replaced.
        // old value in subMethodTable is replaced.
        public CollectSubVisitor(TestClassTable rootClassTable,
                TestClassTable subClassTable, TestMethodTable subMethodTable) {
            this.rootClassTable = rootClassTable;
            this.subClassTable = subClassTable;
            this.subMethodTable = subMethodTable;
        }

        @Override
        public boolean visit(MethodDeclaration node) {
            IMethodBinding methodBinding = node.resolveBinding();
            if (!isSubMethod(methodBinding)) {
                return super.visit(node);
            }

            ITypeBinding classBinding = methodBinding.getDeclaringClass();
            if (!classBinding.isClass() && !classBinding.isInterface()) {
                // enum method, etc
                return super.visit(node);
            }

            TestClass testClass = rootClassTable.getByKey(classBinding.getQualifiedName());
            if (testClass == null) {
                testClass = subClassTable.getByKey(classBinding.getQualifiedName());
                if (testClass == null) {
                    Pair<String, Boolean> pair = getTestDoc(classBinding);
                    if (pair.getRight()) {
                        testClass = new PageClass();
                    } else {
                        testClass = new TestClass();
                    }
                    testClass.setKey(classBinding.getQualifiedName());
                    testClass.setQualifiedName(classBinding.getQualifiedName());
                    testClass.setTestDoc(pair.getLeft());
                    subClassTable.addTestClass(testClass);
                }
            }

            TestMethod testMethod = new TestMethod();
            testMethod.setKey(methodBinding.getKey());
            testMethod.setSimpleName(methodBinding.getName());
            Pair<String, CaptureStyle> pair = getTestDoc(methodBinding);
            testMethod.setTestDoc(pair.getLeft());
            testMethod.setCaptureStyle(pair.getRight());
            for (Object element : node.parameters()) {
                if (!(element instanceof SingleVariableDeclaration)) {
                    throw new RuntimeException("not supported yet: " + element);
                }
                SingleVariableDeclaration varDecl = (SingleVariableDeclaration)element;
                testMethod.addArgVariable(varDecl.getName().getIdentifier());
                if (varDecl.isVarargs()) {
                    testMethod.setVariableLengthArgIndex(testMethod.getArgVariables().size() - 1);
                }
            }
            testMethod.setTestClassKey(testClass.getKey());
            testMethod.setTestClass(testClass);
            subMethodTable.addTestMethod(testMethod);

            testClass.addTestMethodKey(testMethod.getKey());
            testClass.addTestMethod(testMethod);

            return super.visit(node);
        }
    }

    private class CollectSubRequestor extends FileASTRequestor {
        private TestClassTable subClassTable;
        private TestMethodTable subMethodTable;
        private TestClassTable rootClassTable;

        public CollectSubRequestor(TestClassTable rootClassTable) {
            this.rootClassTable = rootClassTable;
            subClassTable = new TestClassTable();
            subMethodTable = new TestMethodTable();
        }

        public TestClassTable getSubClassTable() {
            return subClassTable;
        }

        public TestMethodTable getSubMethodTable() {
            return subMethodTable;
        }

        @Override
        public void acceptAST(String sourceFilePath, CompilationUnit ast) {
            ast.accept(new CollectSubVisitor(
                    rootClassTable, subClassTable, subMethodTable));
        }
    }

    private static String additionalMethodTestDocKey(IMethodBinding method) {
        ITypeBinding[] paramTypes;
        if (method.isParameterizedMethod()) {
            // Use generic type to get argument class types
            // instead of the actual type resolved by JDT.
            IMethodBinding originalMethod = method.getMethodDeclaration();
            paramTypes = originalMethod.getParameterTypes();
        } else {
            paramTypes = method.getParameterTypes();
        }
        List<String> argClassQualifiedNames = new ArrayList<String>(paramTypes.length);
        for (ITypeBinding param : paramTypes) {
            // AdditionalTestDoc's argClassQualifiedNames are defined by type erasure.
            // TODO is this generic handling logic always work well??
            argClassQualifiedNames.add(param.getErasure().getQualifiedName());
        }
        return AdditionalMethodTestDoc.generateMethodKey(
                method.getDeclaringClass().getQualifiedName(), method.getName(), argClassQualifiedNames);
    }

    private static String additionalMethodTestDocKeyNoOverload(IMethodBinding method) {
        return AdditionalMethodTestDoc.generateMethodKey(
                method.getDeclaringClass().getQualifiedName(), method.getName());
    }

    private class CollectCodeVisitor extends ASTVisitor {
        private TestMethodTable rootMethodTable;
        private TestMethodTable subMethodTable;
        private CompilationUnit compilationUnit;

        // set code information to method table
        public CollectCodeVisitor(TestMethodTable rootMethodTable, TestMethodTable subMethodTable,
                CompilationUnit compilationUnit) {
            this.rootMethodTable = rootMethodTable;
            this.subMethodTable = subMethodTable;
            this.compilationUnit = compilationUnit;
        }

        private Code methodBindingCode(IMethodBinding binding,
                Expression thisInstance, List<?> arguments, String original, TestMethod parentMethod) {
            if (binding == null) {
                UnknownCode unknownCode = new UnknownCode();
                unknownCode.setOriginal(original);
                return unknownCode;
            }

            String uniqueKey = binding.getKey();
            TestMethod invocationMethod = subMethodTable.getByKey(uniqueKey);
            if (invocationMethod == null) {
                // TODO What does binding.getName method return for constructor method??
                // TODO key for additional doc should be calculated by the same rule as getKey method in jdt
                String additionalUniqueKeyNoOverload = additionalMethodTestDocKeyNoOverload(binding);
                invocationMethod = subMethodTable.getByKey(additionalUniqueKeyNoOverload);
                if (invocationMethod == null) {
                    String additionalUniqueKey = additionalMethodTestDocKey(binding);
                    invocationMethod = subMethodTable.getByKey(additionalUniqueKey);
                    if (invocationMethod == null) {
                        UnknownCode unknownCode = new UnknownCode();
                        unknownCode.setOriginal(original);
                        return unknownCode;
                    }
                }
            }

            SubMethodInvoke subMethodInvoke = new SubMethodInvoke();
            subMethodInvoke.setSubMethodKey(invocationMethod.getKey());
            subMethodInvoke.setSubMethod(invocationMethod);
            if (thisInstance == null) {
                subMethodInvoke.setThisInstance(null);
            } else {
                subMethodInvoke.setThisInstance(expressionCode(thisInstance, parentMethod));
            }
            for (Object arg : arguments) {
                Expression exp = (Expression) arg;
                subMethodInvoke.addArg(expressionCode(exp, parentMethod));
            }
            subMethodInvoke.setOriginal(original);
            return subMethodInvoke;
        }

        private Code generateParamVarCode(Expression expression,
                IVariableBinding paramVarBinding, TestMethod parentMethod) {
            int argIndex;
            if (parentMethod == null) {
                argIndex = -1;
            } else {
                String varName = paramVarBinding.getName();
                argIndex = parentMethod.getArgVariables().indexOf(varName);
            }

            if (argIndex == -1) {
                // when fails to resolve parameter variable
                return generateUnknownCode(expression);
            }

            MethodArgument methodArg = new MethodArgument();
            methodArg.setOriginal(expression.toString().trim());
            methodArg.setArgIndex(argIndex);
            return methodArg;
        }

        private UnknownCode generateUnknownCode(Expression expression) {
            UnknownCode unknownCode = new UnknownCode();
            unknownCode.setOriginal(expression.toString().trim());
            return unknownCode;
        }

        private Code expressionCode(Expression expression, TestMethod parentMethod) {
            if (expression == null) {
                StringCode strCode = new StringCode();
                strCode.setValue(null);
                strCode.setOriginal("null");
                return strCode;
            } else if (expression instanceof StringLiteral) {
                StringCode strCode = new StringCode();
                strCode.setValue(((StringLiteral) expression).getLiteralValue());
                strCode.setOriginal(expression.toString().trim());
                return strCode;
            } else if (expression instanceof Assignment) {
                Assignment assignment = (Assignment) expression;
                return expressionCode(assignment.getRightHandSide(), parentMethod);
            } else if (expression instanceof MethodInvocation) {
                MethodInvocation invocation = (MethodInvocation) expression;
                IMethodBinding binding = invocation.resolveMethodBinding();
                return methodBindingCode(binding, invocation.getExpression(),
                        invocation.arguments(), expression.toString().trim(), parentMethod);
            } else if (expression instanceof ClassInstanceCreation) {
                ClassInstanceCreation creation = (ClassInstanceCreation) expression;
                IMethodBinding binding = creation.resolveConstructorBinding();
                return methodBindingCode(binding, null, creation.arguments(),
                        expression.toString().trim(), parentMethod);
            } else if (expression instanceof SimpleName) {
               SimpleName simpleName = (SimpleName) expression;
               IBinding binding = simpleName.resolveBinding();
               if (binding instanceof IVariableBinding) {
                   IVariableBinding varBinding = (IVariableBinding) binding;
                   if (varBinding.isParameter()) {
                       return generateParamVarCode(expression, varBinding, parentMethod);
                   } else {
                       return generateUnknownCode(expression);
                   }
               } else {
                   return generateUnknownCode(expression);
               }
            } else{
                return generateUnknownCode(expression);
            }
        }

        @Override
        public boolean visit(MethodDeclaration node) {
            TestMethod testMethod;
            IMethodBinding methodBinding = node.resolveBinding();
            if (AdapterContainer.globalInstance().isRootMethod(methodBinding)) {
                testMethod = rootMethodTable.getByKey(methodBinding.getKey());
            } else if (isSubMethod(methodBinding)) {
                testMethod = subMethodTable.getByKey(methodBinding.getKey());
            } else {
                return super.visit(node);
            }

            Block body = node.getBody();
            if (body == null) {
                // no body. Maybe abstract method or interface method
                return super.visit(node);
            }
            List<?> list = body.statements();
            for (Object obj : list) {
                assert obj instanceof ASTNode;
                ASTNode statementNode = (ASTNode) obj;
                Code code;
                if (statementNode instanceof ExpressionStatement) {
                    Expression expression = ((ExpressionStatement)statementNode).getExpression();
                    code = expressionCode(expression, testMethod);
                } else if (statementNode instanceof VariableDeclarationStatement) {
                    // TODO assume single VariableDeclarationFragment
                    VariableDeclarationFragment varFrag
                    = (VariableDeclarationFragment)(((VariableDeclarationStatement)statementNode).fragments().get(0));
                    Expression expression = varFrag.getInitializer();
                    code = expressionCode(expression, testMethod);
                } else {
                    code = new UnknownCode();
                    code.setOriginal(statementNode.toString().trim());
                }

                CodeLine codeLine = new CodeLine();
                codeLine.setStartLine(compilationUnit.getLineNumber(statementNode.getStartPosition()));
                codeLine.setEndLine(compilationUnit.getLineNumber(
                        statementNode.getStartPosition() + statementNode.getLength()));
                codeLine.setCode(code);
                // sometimes original value set by expressionCode method does not equal to the on of statementNode
                code.setOriginal(statementNode.toString().trim());
                testMethod.addCodeBody(codeLine);
            }
            return super.visit(node);
        }
    }

    private class CollectCodeRequestor extends FileASTRequestor {
        private TestMethodTable rootMethodTable;
        private TestMethodTable subMethodTable;

        public CollectCodeRequestor(TestMethodTable rootMethodTable, TestMethodTable subMethodTable) {
            this.rootMethodTable = rootMethodTable;
            this.subMethodTable = subMethodTable;
        }

        @Override
        public void acceptAST(String sourceFilePath, CompilationUnit ast) {
            ast.accept(new CollectCodeVisitor(rootMethodTable, subMethodTable, ast));
        }
    }

    // srcFiles..parse target files
    // srcEncoding.. encoding of srcFiles. "UTF-8" etc
    // classPathEntries.. all class paths (class file containing directory or jar file) srcFiles depend.
    // this path value is similar to --classpath command line argument, but you must give
    // all class containing sub directories even if the class is in a named package
    public SrcTree generate(String[] srcFiles, String srcEncoding, String[] classPathEntries) {
        // collect root class and method table without code body
        CollectRootRequestor rootRequestor = new CollectRootRequestor();
        parseAST(srcFiles, srcEncoding, classPathEntries, rootRequestor);

        // collect sub class and method table without code body
        CollectSubRequestor subRequestor = new CollectSubRequestor(rootRequestor.getRootClassTable());
        parseAST(srcFiles, srcEncoding, classPathEntries, subRequestor);

        // add not used additional TestDoc to the table
        AdditionalTestDocsSetter setter = new AdditionalTestDocsSetter(
                rootRequestor.getRootClassTable(), subRequestor.getSubClassTable(),
                rootRequestor.getRootMethodTable(), subRequestor.getSubMethodTable());
        setter.set(additionalTestDocs);

        // collect code
        CollectCodeRequestor codeRequestor = new CollectCodeRequestor(
                rootRequestor.getRootMethodTable(), subRequestor.getSubMethodTable());
        parseAST(srcFiles, srcEncoding, classPathEntries, codeRequestor);

        SrcTree result = new SrcTree();
        result.setRootClassTable(rootRequestor.getRootClassTable());
        result.setSubClassTable(subRequestor.getSubClassTable());
        result.setRootMethodTable(rootRequestor.getRootMethodTable());
        result.setSubMethodTable(subRequestor.getSubMethodTable());
        return result;
    }

    private void addToClassPathListFromJarManifest(List<String> classPathList, File jarFile) {
        if (!jarFile.exists()) {
            return; // do nothing
        }
        Manifest manifest = CommonUtils.readManifestFromExternalJar(jarFile);
        // jar class path is sometimes not set at java.class.path property
        // (this case happens for Maven surefire plug-in.
        //  see http://maven.apache.org/surefire/maven-surefire-plugin/examples/class-loading.html)
        String jarClassPathStr = manifest.getMainAttributes().getValue("Class-Path");
        if (jarClassPathStr != null) {
            String[] jarClassPathArray = jarClassPathStr.split(" "); // separator is space character
            addToClassPathList(classPathList, jarClassPathArray);
        }
    }

    private void addToClassPathList(List<String> classPathList, String[] classPathArray) {
        for (String classPath : classPathArray) {
            if (classPath == null || classPath.trim().equals("")) {
                continue;
            }
            String classPathWithoutPrefix;
            if (classPath.startsWith("file:")) {
                // class path in the jar MANIFEST sometimes has this form of class path
                classPathWithoutPrefix = classPath.substring(5);
            } else {
                classPathWithoutPrefix = classPath;
            }
            String absClassPath = new File(classPathWithoutPrefix).getAbsolutePath();

            if (absClassPath.endsWith(".jar")) {
                if (!classPathList.contains(absClassPath)) {
                    classPathList.add(absClassPath);
                    addToClassPathListFromJarManifest(classPathList, new File(absClassPath));
                }
            } else if (absClassPath.endsWith(".zip")) {
                if (!classPathList.contains(absClassPath)) {
                    classPathList.add(absClassPath);
                }
            } else {
                File classPathFile = new File(absClassPath);
                if (classPathFile.isDirectory()) {
                    // needs to add all sub directories
                    // since SrcTreeGenerator does not search classPathEntry sub directories
                    // TODO should add jar file in the sub directories ??
                    Collection<File> subDirCollection = FileUtils.listFilesAndDirs(
                            classPathFile, FileFilterUtils.directoryFileFilter(), FileFilterUtils.trueFileFilter());
                    for (File subDir : subDirCollection) {
                        if (!classPathList.contains(subDir.getAbsolutePath())) {
                            classPathList.add(subDir.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

    public SrcTree generateWithRuntimeClassPath(File srcRootDir, String srcEncoding)
            throws IllegalTestScriptException {
        // set up srcFilePaths
        if (!srcRootDir.exists()) {
            throw new IllegalArgumentException("directory does not exist: " + srcRootDir.getAbsolutePath());
        }
        Collection<File> srcFileCollection = FileUtils.listFiles(srcRootDir, new String[]{"java"}, true);
        List<File> srcFileList = new ArrayList<File>(srcFileCollection);
        String[] srcFilePaths = new String[srcFileList.size()];
        for (int i = 0; i < srcFileList.size(); i++) {
            srcFilePaths[i] = srcFileList.get(i).getAbsolutePath();
        }

        // set up classPathEntries
        // TODO handle wild card classpath entry
        List<String> classPathList = new ArrayList<String>(64);
        String classPathStr = System.getProperty("java.class.path");
        String[] classPathArray = classPathStr.split(Pattern.quote(File.pathSeparator));
        addToClassPathList(classPathList, classPathArray);
        for (String classPath : classPathList) {
            logger.info("classPath: " + classPath);
        }
        return generate(srcFilePaths, srcEncoding, classPathList.toArray(new String[0]));
    }

}
