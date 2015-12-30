package org.sahagin.runlib.srctreegen;

import java.io.File;
import java.nio.charset.Charset;
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
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.sahagin.runlib.additionaltestdoc.AdditionalClassTestDoc;
import org.sahagin.runlib.additionaltestdoc.AdditionalMethodTestDoc;
import org.sahagin.runlib.additionaltestdoc.AdditionalPage;
import org.sahagin.runlib.additionaltestdoc.AdditionalTestDocs;
import org.sahagin.runlib.external.CaptureStyle;
import org.sahagin.runlib.external.TestStepLabelMethod;
import org.sahagin.runlib.external.adapter.JavaAdapterContainer;
import org.sahagin.share.AcceptableLocales;
import org.sahagin.share.CommonUtils;
import org.sahagin.share.IllegalTestScriptException;
import org.sahagin.share.Logging;
import org.sahagin.share.srctree.PageClass;
import org.sahagin.share.srctree.SrcTree;
import org.sahagin.share.srctree.TestClass;
import org.sahagin.share.srctree.TestClassTable;
import org.sahagin.share.srctree.TestField;
import org.sahagin.share.srctree.TestFieldTable;
import org.sahagin.share.srctree.TestMethod;
import org.sahagin.share.srctree.TestMethodTable;
import org.sahagin.share.srctree.code.Code;
import org.sahagin.share.srctree.code.CodeLine;
import org.sahagin.share.srctree.code.Field;
import org.sahagin.share.srctree.code.LocalVar;
import org.sahagin.share.srctree.code.MethodArgument;
import org.sahagin.share.srctree.code.StringCode;
import org.sahagin.share.srctree.code.SubMethodInvoke;
import org.sahagin.share.srctree.code.TestStepLabel;
import org.sahagin.share.srctree.code.UnknownCode;
import org.sahagin.share.srctree.code.VarAssign;

import static org.sahagin.runlib.external.adapter.javasystem.JavaSystemAdditionalTestDocsAdapter.*;

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
        String pageDoc = ASTUtils.getPageDoc(type, locales);
        if (pageDoc != null) {
            return Pair.of(pageDoc, true);
        }

        String testDoc = ASTUtils.getTestDoc(type, locales);
        if (testDoc != null) {
            return Pair.of(testDoc, false);
        }
        AdditionalClassTestDoc additional
        = additionalTestDocs.getClassTestDoc(type.getBinaryName());
        if (additional != null) {
            return Pair.of(additional.getTestDoc(), additional instanceof AdditionalPage);
        }
        return Pair.of(null, false);
    }

    // null and default CaptureStyle pair if not found
    private Pair<String, CaptureStyle> getTestDoc(IMethodBinding method) {
        // TODO additional TestDoc should be prior to annotation TestDoc !?
        Pair<String, CaptureStyle> pair = ASTUtils.getTestDoc(method, locales);
        if (pair.getLeft() != null) {
            return pair;
        }

        List<String> argClassQualifiedNames = getArgClassQualifiedNames(method);
        AdditionalMethodTestDoc additional = additionalTestDocs.getMethodTestDoc(
                method.getDeclaringClass().getBinaryName(), method.getName(), argClassQualifiedNames);
        if (additional != null) {
            return Pair.of(additional.getTestDoc(), additional.getCaptureStyle());
        }
        return Pair.of(null, CaptureStyle.getDefault());
    }

    // returns null and default CaptureStyle pair if the method is not sub method
    private Pair<String, CaptureStyle> testDocIfSubMethod(IMethodBinding methodBinding) {
        // rootMethod also can have its TestDoc value
        if (JavaAdapterContainer.globalInstance().isRootMethod(methodBinding)) {
            return Pair.of(null, CaptureStyle.getDefault());
        }
        return getTestDoc(methodBinding);
    }

    private IVariableBinding getVariableBinding(FieldDeclaration declaration) {
        for (Object fragment : declaration.fragments()) {
            if (fragment instanceof VariableDeclarationFragment) {
                VariableDeclarationFragment varDecl = (VariableDeclarationFragment) fragment;
                IVariableBinding binding = varDecl.resolveBinding();
                if (binding != null) {
                    return binding;
                }
            }
        }
        return null;
    }

    private List<String> getArgClassQualifiedNames(IMethodBinding method) {
        ITypeBinding[] paramTypes;
        if (method.isParameterizedMethod()) {
            // Use generic type to get argument class types
            // instead of the actual type resolved by JDT.
            IMethodBinding originalMethod = method.getMethodDeclaration();
            paramTypes = originalMethod.getParameterTypes();
        } else {
            paramTypes = method.getParameterTypes();
        }

        List<String> result = new ArrayList<String>(paramTypes.length);
        for (ITypeBinding param : paramTypes) {
            // AdditionalTestDoc's argClassQualifiedNames are defined by type erasure.
            // TODO is this generic handling logic always work well??
            ITypeBinding erasure = param.getErasure();
            if (erasure.isPrimitive() || erasure.isArray()) {
                // "int", "boolean", etc
                result.add(erasure.getQualifiedName());
            } else {
                // getBinaryName and getQualifiedName are not the same.
                // For example, getBinaryName returns parent$child for inner class,
                // but getQualifiedName returns parent.child
                result.add(erasure.getBinaryName());
            }
        }
        return result;
    }

    private String generateMethodKey(IMethodBinding method, boolean noArgClassesStr) {
        String classQualifiedName = method.getDeclaringClass().getBinaryName();
        String methodSimpleName = method.getName();
        List<String> argClassQualifiedNames = getArgClassQualifiedNames(method);
        if (noArgClassesStr) {
            return TestMethod.generateMethodKey(classQualifiedName, methodSimpleName);
        } else {
            return TestMethod.generateMethodKey(
                    classQualifiedName, methodSimpleName, argClassQualifiedNames);
        }
    }

    // srcFiles..parse target files
    // classPathEntries.. all class paths (class file containing directory or jar file) srcFiles depend
    private static void parseAST(String[] srcFiles, Charset srcCharset,
            String[] classPathEntries, FileASTRequestor requestor) {
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
            srcEncodings[i] = srcCharset.name();
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
            if (!JavaAdapterContainer.globalInstance().isRootMethod(methodBinding)) {
                return super.visit(node);
            }

            ITypeBinding classBinding = methodBinding.getDeclaringClass();
            if (!classBinding.isClass() && !classBinding.isInterface()) {
                // enum method, etc
                return super.visit(node);
            }

            TestClass rootClass = rootClassTable.getByKey(classBinding.getBinaryName());
            if (rootClass == null) {
                Pair<String, Boolean> pair = getTestDoc(classBinding);
                if (pair.getRight()) {
                    rootClass = new PageClass(); // though root class cannot be page class
                } else {
                    rootClass = new TestClass();
                }
                rootClass.setKey(classBinding.getBinaryName());
                rootClass.setQualifiedName(classBinding.getBinaryName());
                rootClass.setTestDoc(pair.getLeft());
                rootClassTable.addTestClass(rootClass);
            }

            TestMethod testMethod = new TestMethod();
            testMethod.setKey(generateMethodKey(methodBinding, false));
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
        private TestFieldTable fieldTable;

        // rootClassTable if only for read, not write any data.
        // old value in subClassTable is not replaced.
        // old value in subMethodTable and fieldTable is replaced.
        public CollectSubVisitor(TestClassTable rootClassTable,
                TestClassTable subClassTable, TestMethodTable subMethodTable, TestFieldTable fieldTable) {
            this.rootClassTable = rootClassTable;
            this.subClassTable = subClassTable;
            this.subMethodTable = subMethodTable;
            this.fieldTable = fieldTable;
        }

        // Returns existing TestClass in rootClassTable or subClassTable if found in these table.
        // If not found, create new TestClass and add it to subClassTable and returns it
        private TestClass classBindingTestClass(ITypeBinding classBinding) {
            TestClass testClass = rootClassTable.getByKey(classBinding.getBinaryName());
            if (testClass == null) {
                testClass = subClassTable.getByKey(classBinding.getBinaryName());
                if (testClass == null) {
                    Pair<String, Boolean> pair = getTestDoc(classBinding);
                    if (pair.getRight()) {
                        testClass = new PageClass();
                    } else {
                        testClass = new TestClass();
                    }
                    testClass.setKey(classBinding.getBinaryName());
                    testClass.setQualifiedName(classBinding.getBinaryName());
                    testClass.setTestDoc(pair.getLeft());
                    subClassTable.addTestClass(testClass);
                }
            }
            return testClass;
        }

        @Override
        public boolean visit(FieldDeclaration node) {
            IVariableBinding variable = getVariableBinding(node);
            if (variable == null) {
                return super.visit(node);
            }
            ITypeBinding classBinding = variable.getDeclaringClass();
            if (!classBinding.isClass() && !classBinding.isInterface()) {
                // enum method, etc
                return super.visit(node);
            }

            // TODO support additional testDoc for Field
            String testDoc = ASTUtils.getTestDoc(variable, locales);
            if (testDoc == null) {
                return super.visit(node);
            }

            TestClass testClass = classBindingTestClass(classBinding);
            TestField testField = new TestField();
            testField.setTestClassKey(testClass.getKey());
            testField.setTestClass(testClass);
            testField.setKey(testClass.getKey() + "." + variable.getName());
            testField.setSimpleName(variable.getName());
            testField.setTestDoc(testDoc);
            testField.setValue(null); // TODO currently not supported
            fieldTable.addTestField(testField);

            testClass.addTestFieldKey(testField.getKey());
            testClass.addTestField(testField);

            return super.visit(node);
        }

        @Override
        public boolean visit(MethodDeclaration node) {
            IMethodBinding methodBinding = node.resolveBinding();
            Pair<String, CaptureStyle> testDocPair = testDocIfSubMethod(methodBinding);
            if (testDocPair.getLeft() == null) {
                return super.visit(node);
            }

            ITypeBinding classBinding = methodBinding.getDeclaringClass();
            if (!classBinding.isClass() && !classBinding.isInterface()) {
                // enum method, etc
                return super.visit(node);
            }

            TestClass testClass = classBindingTestClass(classBinding);

            TestMethod testMethod = new TestMethod();
            testMethod.setKey(generateMethodKey(methodBinding, false));
            testMethod.setSimpleName(methodBinding.getName());
            testMethod.setTestDoc(testDocPair.getLeft());
            testMethod.setCaptureStyle(testDocPair.getRight());
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
        private TestFieldTable fieldTable;

        public CollectSubRequestor(TestClassTable rootClassTable) {
            this.rootClassTable = rootClassTable;
            subClassTable = new TestClassTable();
            subMethodTable = new TestMethodTable();
            fieldTable = new TestFieldTable();
        }

        public TestClassTable getSubClassTable() {
            return subClassTable;
        }

        public TestMethodTable getSubMethodTable() {
            return subMethodTable;
        }

        public TestFieldTable getFieldTable() {
            return fieldTable;
        }

        @Override
        public void acceptAST(String sourceFilePath, CompilationUnit ast) {
            ast.accept(new CollectSubVisitor(
                    rootClassTable, subClassTable, subMethodTable, fieldTable));
        }
    }

    private class CollectCodeVisitor extends ASTVisitor {
        private TestClassTable subClassTable;
        private TestMethodTable rootMethodTable;
        private TestMethodTable subMethodTable;
        private TestFieldTable fieldTable;
        private CompilationUnit compilationUnit;

        // set code information to method table
        public CollectCodeVisitor(TestClassTable subClassTable,
                TestMethodTable rootMethodTable, TestMethodTable subMethodTable,
                TestFieldTable fieldTable, CompilationUnit compilationUnit) {
            this.subClassTable = subClassTable;
            this.rootMethodTable = rootMethodTable;
            this.subMethodTable = subMethodTable;
            this.fieldTable = fieldTable;
            this.compilationUnit = compilationUnit;
        }

        private TestMethod getTestMethod(IMethodBinding method) {
            TestMethod testMethod = subMethodTable.getByKey(generateMethodKey(method, false));
            if (testMethod != null) {
                return testMethod;
            }
            testMethod = subMethodTable.getByKey(generateMethodKey(method, true));
            if (testMethod != null) {
                return testMethod;
            }
            return null;
        }

        // search super method of the specified method
        // from the specified type and its super class and implementing interface recursively.
        // type: class or interface
        // superOnly: if true, does not check the specified type itself
        private TestMethod getSuperMethodSub(
                ITypeBinding type, IMethodBinding method, boolean superOnly) {
            if (type == null) {
                return null;
            }
            if (!superOnly) {
                for (IMethodBinding declaredMethod : type.getDeclaredMethods()) {
                    if (method.overrides(declaredMethod)) {
                        TestMethod testMethod = getTestMethod(declaredMethod);
                        if (testMethod != null) {
                            return testMethod;
                        }
                    }
                }
            }
            TestMethod testMethod = getSuperMethodSub(type.getSuperclass(), method, false);
            if (testMethod != null) {
                return testMethod;
            }
            for (ITypeBinding implInterface : type.getInterfaces()) {
                testMethod = getSuperMethodSub(implInterface, method, false);
                if (testMethod != null) {
                    return testMethod;
                }
            }
            return null;
        }

        // First found super method of the specified method.
        // returns null if not found
        private TestMethod getSuperMethod(IMethodBinding method) {
            return getSuperMethodSub(method.getDeclaringClass(), method, true);
        }

        private Code generateMethodInvokeCode(IMethodBinding binding,
                Expression thisInstance, List<?> arguments, String original, TestMethod parentMethod) {
            if (binding == null) {
                return generateUnknownCode(original);
            }

            boolean childInvoke = false;
            TestMethod invocationMethod = getTestMethod(binding);
            if (invocationMethod == null) {
                invocationMethod = getSuperMethod(binding);
                if (invocationMethod != null) {
                    childInvoke = true;
                }
            }

            if (invocationMethod == null) {
                return generateUnknownCode(original);
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
            subMethodInvoke.setChildInvoke(childInvoke);
            subMethodInvoke.setOriginal(original);
            return subMethodInvoke;
        }

        private Code generateMethodArgCode(SimpleName simpleName,
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
                return generateUnknownCode(simpleName);
            }

            MethodArgument methodArg = new MethodArgument();
            methodArg.setOriginal(simpleName.toString().trim());
            methodArg.setArgIndex(argIndex);
            return methodArg;
        }

        private Code generateFieldCode(SimpleName simpleName,
                IVariableBinding localVarBinding) {
            String key = localVarBinding.getDeclaringClass().getBinaryName()
                    + "." + localVarBinding.getName();
            TestField testField = fieldTable.getByKey(key);
            if (testField == null) {
                return generateUnknownCode(simpleName.toString().trim());
            }
            Field field = new Field();
            field.setFieldKey(testField.getKey());
            field.setField(testField);
            field.setOriginal(simpleName.toString().trim());
            field.setThisInstance(null); // TODO really null??
            return field;
        }

        private LocalVar generateLocalVarCode(SimpleName simpleName,
                IVariableBinding localVarBinding) {
            LocalVar localVar = new LocalVar();
            localVar.setOriginal(simpleName.toString().trim());
            localVar.setName(simpleName.getIdentifier());
            return localVar;
        }

        // expression is used to get original code, and can be null
        private Code generateLocalVarAssignCode(Expression expression,
                Expression left, Expression right, TestMethod parentMethod) {
            Code rightCode = expressionCode(right, parentMethod);
            if (rightCode instanceof UnknownCode) {
                // ignore left for UnknownCode assignment
                return rightCode;
            }
            if (!(left instanceof SimpleName)) {
                return rightCode; // ignore left
            }
            SimpleName simpleName = (SimpleName) left;
            IBinding leftBinding = simpleName.resolveBinding();
            if (!(leftBinding instanceof IVariableBinding)) {
                return rightCode; // ignore left
            }
            IVariableBinding varBinding = (IVariableBinding) leftBinding;
            if (varBinding.isField() || varBinding.isParameter()) {
                // ignore left for field assignment and method argument assignment
                // TODO should handle field assignment as VarAssign for Field ??
                return rightCode;
            }

            String classKey = varBinding.getType().getBinaryName();
            TestClass subClass = subClassTable.getByKey(classKey);
            if (subClass != null && subClass instanceof PageClass) {
                // ignore left for page type variable assignment
                // since usually page type variable is not used in other TestDoc
                return rightCode;
            }

            LocalVar localVar = generateLocalVarCode(simpleName, varBinding);
            VarAssign assign = new VarAssign();
            assign.setOriginal(expression.toString().trim());
            assign.setVariable(localVar);
            assign.setValue(rightCode);
            return assign;
        }

        // return null if method does not represent TestStepLabel
        private TestStepLabel generateTestStepLabelCode(
                MethodInvocation invocation, TestMethod parentMethod) {
            IMethodBinding method = invocation.resolveMethodBinding();
            if (method == null) {
                return null;
            }
            String methodName = method.getName();
            if (methodName == null || !methodName.equals("TestDoc")) {
                return null;
            }

            ITypeBinding defClass = method.getDeclaringClass();
            if (defClass == null
                    || defClass.getQualifiedName() == null
                    || !defClass.getQualifiedName().equals(TestStepLabelMethod.class.getCanonicalName())) {
                return null;
            }
            assert invocation.arguments().size() == 1;
            Expression arg = (Expression) invocation.arguments().get(0);
            Code argCode = expressionCode(arg, parentMethod);
            if (!(argCode instanceof StringCode)) {
                throw new RuntimeException(
                        "testDoc method argument must be string literal argument at this moment");
            }

            TestStepLabel stepLabel = new TestStepLabel();
            stepLabel.setLabel(null);
            stepLabel.setText(((StringCode) argCode).getValue());
            return stepLabel;
        }

        private SubMethodInvoke generateAssertMethodInvokeCode(
                Expression expression, String original, TestMethod parentMethod) {
            String assertMethodKey = TestMethod.generateMethodKey(CLASS_QUALIFIED_NAME, METHOD_ASSERT);
            TestMethod assertMethod = subMethodTable.getByKey(assertMethodKey);
            assert assertMethod != null;
            SubMethodInvoke assertMethodInvoke = new SubMethodInvoke();
            assertMethodInvoke.setSubMethodKey(assertMethodKey);
            assertMethodInvoke.setSubMethod(assertMethod);
            assertMethodInvoke.addArg(expressionCode(expression, parentMethod));
            assertMethodInvoke.setOriginal(original);
            return assertMethodInvoke;
        }

        private Code generateInfixMethodInvokeCode(InfixExpression infix, TestMethod parentMethod) {
            String infixMethodKey;
            String operator = infix.getOperator().toString();
            if (operator.equals(InfixExpression.Operator.EQUALS.toString())) {
                infixMethodKey = TestMethod.generateMethodKey(CLASS_QUALIFIED_NAME, METHOD_EQUALS);
            } else if (operator.equals(InfixExpression.Operator.NOT_EQUALS.toString())) {
                infixMethodKey = TestMethod.generateMethodKey(CLASS_QUALIFIED_NAME, METHOD_NOT_EQUALS);
            } else {
                return generateUnknownCode(infix);
            }

            TestMethod infixMethod = subMethodTable.getByKey(infixMethodKey);
            assert infixMethod != null;
            SubMethodInvoke infixMethodInvoke = new SubMethodInvoke();
            infixMethodInvoke.setSubMethodKey(infixMethodKey);
            infixMethodInvoke.setSubMethod(infixMethod);
            Code leftCode = expressionCode(infix.getLeftOperand(), parentMethod);
            Code rightcode = expressionCode(infix.getRightOperand(), parentMethod);
            infixMethodInvoke.addArg(leftCode);
            infixMethodInvoke.addArg(rightcode);
            infixMethodInvoke.setOriginal(infix.toString().trim());
            return infixMethodInvoke;
        }

        private UnknownCode generateUnknownCode(String original) {
            UnknownCode unknownCode = new UnknownCode();
            unknownCode.setOriginal(original);
            return unknownCode;
        }

        private UnknownCode generateUnknownCode(Expression expression) {
            return generateUnknownCode(expression.toString().trim());
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
                return generateLocalVarAssignCode(expression, assignment.getLeftHandSide(),
                        assignment.getRightHandSide(), parentMethod);
            } else if (expression instanceof MethodInvocation) {
                MethodInvocation invocation = (MethodInvocation) expression;
                TestStepLabel stepLabel = generateTestStepLabelCode(invocation, parentMethod);
                if (stepLabel != null) {
                    return stepLabel;
                }
                IMethodBinding binding = invocation.resolveMethodBinding();
                return generateMethodInvokeCode(binding, invocation.getExpression(),
                        invocation.arguments(), expression.toString().trim(), parentMethod);
            } else if (expression instanceof ClassInstanceCreation) {
                ClassInstanceCreation creation = (ClassInstanceCreation) expression;
                IMethodBinding binding = creation.resolveConstructorBinding();
                return generateMethodInvokeCode(binding, null, creation.arguments(),
                        expression.toString().trim(), parentMethod);
            } else if (expression instanceof SimpleName) {
               SimpleName simpleName = (SimpleName) expression;
               IBinding binding = simpleName.resolveBinding();
               if (binding instanceof IVariableBinding) {
                   IVariableBinding varBinding = (IVariableBinding) binding;
                   if (varBinding.isParameter()) {
                       // method argument
                       return generateMethodArgCode(simpleName, varBinding, parentMethod);
                   } else if (varBinding.isField()) {
                       return generateFieldCode(simpleName, varBinding);
                   } else {
                       // local variable reference
                       return generateLocalVarCode(simpleName, varBinding);
                   }
               } else {
                   return generateUnknownCode(expression);
               }
            } else if (expression instanceof InfixExpression) {
                InfixExpression infix = (InfixExpression) expression;
                return generateInfixMethodInvokeCode(infix, parentMethod);
            } else{
                return generateUnknownCode(expression);
            }
        }

        private CodeLine statementCodeLine(Statement statement, TestMethod parentMethod) {
            Code code;
            if (statement instanceof ExpressionStatement) {
                Expression expression = ((ExpressionStatement) statement).getExpression();
                code = expressionCode(expression, parentMethod);
            } else if (statement instanceof VariableDeclarationStatement) {
                // TODO assume single VariableDeclarationFragment
                VariableDeclarationStatement varDeclStatement = (VariableDeclarationStatement) statement;
                VariableDeclarationFragment varFrag
                = (VariableDeclarationFragment) varDeclStatement.fragments().get(0);
                Expression expression = varFrag.getInitializer();
                if (expression == null) {
                    code = new UnknownCode();
                } else {
                    code = generateLocalVarAssignCode(expression, varFrag.getName(),
                            expression, parentMethod);
                }
            } else if (statement instanceof AssertStatement) {
                Expression expression = ((AssertStatement) statement).getExpression();
                code = generateAssertMethodInvokeCode(expression, statement.toString().trim(), parentMethod);
            } else {
                code = new UnknownCode();
            }

            CodeLine codeLine = new CodeLine();
            codeLine.setStartLine(compilationUnit.getLineNumber(statement.getStartPosition()));
            codeLine.setEndLine(compilationUnit.getLineNumber(
                    statement.getStartPosition() + statement.getLength()));
            codeLine.setCode(code);
            // sometimes original value set by expressionCode method does not equal to the one of statementNode
            code.setOriginal(statement.toString().trim());
            return codeLine;
        }

        @Override
        public boolean visit(MethodDeclaration node) {
            TestMethod testMethod;
            IMethodBinding methodBinding = node.resolveBinding();
            if (JavaAdapterContainer.globalInstance().isRootMethod(methodBinding)) {
                // TODO searching twice from table is not elegant logic..
                testMethod = rootMethodTable.getByKey(generateMethodKey(methodBinding, false));
                if (testMethod == null) {
                    testMethod = rootMethodTable.getByKey(generateMethodKey(methodBinding, true));
                }
            } else if (testDocIfSubMethod(methodBinding).getLeft() != null) {
                // subMethod
                // TODO searching twice from table is not elegant logic..
                testMethod = subMethodTable.getByKey(generateMethodKey(methodBinding, false));
                if (testMethod == null) {
                    testMethod = subMethodTable.getByKey(generateMethodKey(methodBinding, true));
                }
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
                assert obj instanceof Statement;
                CodeLine codeLine = statementCodeLine((Statement) obj, testMethod);
                testMethod.addCodeBody(codeLine);
            }
            return super.visit(node);
        }
    }

    private class CollectCodeRequestor extends FileASTRequestor {
        private TestClassTable subClassTable;
        private TestMethodTable rootMethodTable;
        private TestMethodTable subMethodTable;
        private TestFieldTable fieldTable;

        public CollectCodeRequestor(TestClassTable subClassTable,
                TestMethodTable rootMethodTable, TestMethodTable subMethodTable,
                TestFieldTable fieldTable) {
            this.subClassTable = subClassTable;
            this.rootMethodTable = rootMethodTable;
            this.subMethodTable = subMethodTable;
            this.fieldTable = fieldTable;
        }

        @Override
        public void acceptAST(String sourceFilePath, CompilationUnit ast) {
            ast.accept(new CollectCodeVisitor(
                    subClassTable, rootMethodTable, subMethodTable, fieldTable, ast));
        }
    }

    // srcFiles..parse target files
    // srcCharset.. charset of srcFiles.
    // classPathEntries.. all class paths (class file containing directory or jar file) srcFiles depend.
    // this path value is similar to --classpath command line argument, but you must give
    // all class containing sub directories even if the class is in a named package
    public SrcTree generate(String[] srcFiles, Charset srcCharset, String[] classPathEntries) {
        // collect root class and method table without code body
        CollectRootRequestor rootRequestor = new CollectRootRequestor();
        parseAST(srcFiles, srcCharset, classPathEntries, rootRequestor);

        // collect sub class and method table without code body
        CollectSubRequestor subRequestor = new CollectSubRequestor(rootRequestor.getRootClassTable());
        parseAST(srcFiles, srcCharset, classPathEntries, subRequestor);

        // add additional TestDoc to the table
        AdditionalTestDocsSetter setter = new AdditionalTestDocsSetter(
                rootRequestor.getRootClassTable(), subRequestor.getSubClassTable(),
                rootRequestor.getRootMethodTable(), subRequestor.getSubMethodTable());
        setter.set(additionalTestDocs);

        // collect code
        CollectCodeRequestor codeRequestor = new CollectCodeRequestor(
                subRequestor.getSubClassTable(), rootRequestor.getRootMethodTable(),
                subRequestor.getSubMethodTable(), subRequestor.getFieldTable());
        parseAST(srcFiles, srcCharset, classPathEntries, codeRequestor);

        SrcTree result = new SrcTree();
        result.setRootClassTable(rootRequestor.getRootClassTable());
        result.setSubClassTable(subRequestor.getSubClassTable());
        result.setRootMethodTable(rootRequestor.getRootMethodTable());
        result.setSubMethodTable(subRequestor.getSubMethodTable());
        result.setFieldTable(subRequestor.getFieldTable());
        return result;
    }

    private void addToClassPathListFromJarManifest(List<String> classPathList, File jarFile) {
        if (!jarFile.exists()) {
            return; // do nothing
        }
        Manifest manifest = CommonUtils.readManifestFromExternalJar(jarFile);
        if (manifest == null) {
            return; // just ignore no manifest jar file
        }
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

    public SrcTree generateWithRuntimeClassPath(File srcRootDir, Charset srcCharset)
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
        return generate(srcFilePaths, srcCharset, classPathList.toArray(new String[0]));
    }

}
