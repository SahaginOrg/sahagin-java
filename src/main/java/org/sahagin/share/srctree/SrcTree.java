package org.sahagin.share.srctree;

import java.util.HashMap;
import java.util.Map;

import org.sahagin.share.CommonUtils;
import org.sahagin.share.IllegalDataStructureException;
import org.sahagin.share.srctree.code.ClassInstance;
import org.sahagin.share.srctree.code.Code;
import org.sahagin.share.srctree.code.CodeLine;
import org.sahagin.share.srctree.code.Field;
import org.sahagin.share.srctree.code.SubMethodInvoke;
import org.sahagin.share.srctree.code.TestStep;
import org.sahagin.share.srctree.code.VarAssign;
import org.sahagin.share.yaml.YamlUtils;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlConvertible;

public class SrcTree implements YamlConvertible {
    private static final String MSG_CLASS_NOT_FOUND = "class not found; key: %s";
    private static final String MSG_METHOD_NOT_FOUND = "method not found; key: %s";
    private static final String MSG_FIELD_NOT_FOUND = "field not found; key: %s";
    private static final String MSG_SRC_TREE_FORMAT_MISMATCH
    = "expected formatVersion is \"%s\", but actual is \"%s\"";

    private TestClassTable rootClassTable = new TestClassTable();
    private TestMethodTable rootMethodTable = new TestMethodTable();
    private TestClassTable subClassTable = new TestClassTable();
    private TestMethodTable subMethodTable = new TestMethodTable();
    private TestFieldTable fieldTable = new TestFieldTable();

    public TestClassTable getRootClassTable() {
        return rootClassTable;
    }

    public void setRootClassTable(TestClassTable rootClassTable) {
        if (rootClassTable == null) {
            throw new NullPointerException();
        }
        this.rootClassTable = rootClassTable;
    }

    public TestMethodTable getRootMethodTable() {
        return rootMethodTable;
    }

    public void setRootMethodTable(TestMethodTable rootMethodTable) {
        if (rootMethodTable == null) {
            throw new NullPointerException();
        }
        this.rootMethodTable = rootMethodTable;
    }

    public TestClassTable getSubClassTable() {
        return subClassTable;
    }

    public void setSubClassTable(TestClassTable subClassTable) {
        if (subClassTable == null) {
            throw new NullPointerException();
        }
        this.subClassTable = subClassTable;
    }

    public TestMethodTable getSubMethodTable() {
        return subMethodTable;
    }

    public void setSubMethodTable(TestMethodTable subMethodTable) {
        if (subMethodTable == null) {
            throw new NullPointerException();
        }
        this.subMethodTable = subMethodTable;
    }

    public TestFieldTable getFieldTable() {
        return fieldTable;
    }

    public void setFieldTable(TestFieldTable fieldTable) {
        if (fieldTable == null) {
            throw new NullPointerException();
        }
        this.fieldTable = fieldTable;
    }

    public void sort() {
        rootClassTable.sort();
        rootMethodTable.sort();
        subClassTable.sort();
        subMethodTable.sort();
        fieldTable.sort();
    }

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = new HashMap<>(8);
        result.put("formatVersion", CommonUtils.formatVersion());
        if (!fieldTable.isEmpty()) {
            result.put("fieldTable", fieldTable.toYamlObject());
        }
        if (!rootMethodTable.isEmpty()) {
            result.put("rootMethodTable", rootMethodTable.toYamlObject());
        }
        if (!subMethodTable.isEmpty()) {
            result.put("subMethodTable", subMethodTable.toYamlObject());
        }
        if (!rootClassTable.isEmpty()) {
            result.put("rootClassTable", rootClassTable.toYamlObject());
        }
        if (!subClassTable.isEmpty()) {
            result.put("subClassTable", subClassTable.toYamlObject());
        }
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        String formatVersion = YamlUtils.getStrValue(yamlObject, "formatVersion");
        // "*" means arbitrary version (this is only for testing sahagin itself)
        if (!formatVersion.equals("*")
                && !formatVersion.equals(CommonUtils.formatVersion())) {
            throw new YamlConvertException(String.format
                    (MSG_SRC_TREE_FORMAT_MISMATCH, CommonUtils.formatVersion(), formatVersion));
        }

        fieldTable = new TestFieldTable();
        Map<String, Object> fieldTableYamlObj
        = YamlUtils.getYamlObjectValue(yamlObject, "fieldTable", true);
        if (fieldTableYamlObj != null) {
            fieldTable.fromYamlObject(fieldTableYamlObj);
        }

        rootMethodTable = new TestMethodTable();
        Map<String, Object> rootMethodTableYamlObj
        = YamlUtils.getYamlObjectValue(yamlObject, "rootMethodTable", true);
        if (rootMethodTableYamlObj != null) {
            rootMethodTable.fromYamlObject(rootMethodTableYamlObj);
        }

        subMethodTable = new TestMethodTable();
        Map<String, Object> subMethodTableYamlObj
        = YamlUtils.getYamlObjectValue(yamlObject, "subMethodTable", true);
        if (subMethodTableYamlObj != null) {
            subMethodTable.fromYamlObject(subMethodTableYamlObj);
        }

        rootClassTable = new TestClassTable();
        Map<String, Object> rootClassTableYamlObj
        = YamlUtils.getYamlObjectValue(yamlObject, "rootClassTable", true);
        if (rootClassTableYamlObj != null) {
            rootClassTable.fromYamlObject(rootClassTableYamlObj);
        }

        subClassTable = new TestClassTable();
        Map<String, Object> subClassTableYamlObj
        = YamlUtils.getYamlObjectValue(yamlObject, "subClassTable", true);
        if (subClassTableYamlObj != null) {
            subClassTable.fromYamlObject(subClassTableYamlObj);
        }
    }

    public TestClass getTestClassByKey(String testClassKey, boolean allowsNotFound)
            throws IllegalDataStructureException {
        TestClass subClass = subClassTable.getByKey(testClassKey);
        if (subClass != null) {
            return subClass;
        }
        TestClass rootClass = rootClassTable.getByKey(testClassKey);
        if (rootClass != null) {
            return rootClass;
        }
        if (allowsNotFound) {
            return null;
        } else {
            throw new IllegalDataStructureException(String.format(MSG_CLASS_NOT_FOUND, testClassKey));
        }
    }

    public TestClass getTestClassByKey(String testClassKey) throws IllegalDataStructureException {
        return getTestClassByKey(testClassKey, false);
    }

    public TestMethod getTestMethodByKey(String testMethodKey, boolean allowsNotFound)
            throws IllegalDataStructureException {
        TestMethod subMethod = subMethodTable.getByKey(testMethodKey);
        if (subMethod != null) {
            return subMethod;
        }
        TestMethod rootMethod = rootMethodTable.getByKey(testMethodKey);
        if (rootMethod != null) {
            return rootMethod;
        }
        if (allowsNotFound) {
            return null;
        } else {
            throw new IllegalDataStructureException(String.format(MSG_METHOD_NOT_FOUND, testMethodKey));
        }
    }

    public TestMethod getTestMethodByKey(String testMethodKey) throws IllegalDataStructureException {
        return getTestMethodByKey(testMethodKey, false);
    }

    public TestField getTestFieldByKey(String testFieldKey, boolean allowsNotFound)
            throws IllegalDataStructureException {
        TestField field = fieldTable.getByKey(testFieldKey);
        if (field != null) {
            return field;
        }
        if (allowsNotFound) {
            return null;
        } else {
            throw new IllegalDataStructureException(String.format(MSG_FIELD_NOT_FOUND, testFieldKey));
        }
    }

    public TestField getTestFieldByKey(String testFieldKey) throws IllegalDataStructureException {
        return getTestFieldByKey(testFieldKey, false);
    }

    private void resolveTestClass(TestMethod testMethod) throws IllegalDataStructureException {
        testMethod.setTestClass(getTestClassByKey(testMethod.getTestClassKey()));
    }

    private void resolveTestClass(TestField testField) throws IllegalDataStructureException {
        testField.setTestClass(getTestClassByKey(testField.getTestClassKey()));
    }

    private void resolveTestMethod(TestClass testClass) throws IllegalDataStructureException {
        testClass.clearTestMethods();
        for (String testMethodKey : testClass.getTestMethodKeys()) {
            TestMethod testMethod = getTestMethodByKey(testMethodKey);
            testClass.addTestMethod(testMethod);
        }
    }

    private void resolveTestField(TestClass testClass) throws IllegalDataStructureException {
        testClass.clearTestFields();
        for (String testFieldKey : testClass.getTestFieldKeys()) {
            TestField testField = getTestFieldByKey(testFieldKey);
            testClass.addTestField(testField);
        }
    }

    private void resolveDelegateToTestClass(TestClass testClass)
            throws IllegalDataStructureException {
        if (testClass.getDelegateToTestClassKey() == null) {
            testClass.setDelegateToTestClass(null);
        } else {
            testClass.setDelegateToTestClass(
                    getTestClassByKey(testClass.getDelegateToTestClassKey()));
        }
    }

    private void resolveKeyReferenceInCode(Code code) throws IllegalDataStructureException {
        if (code instanceof SubMethodInvoke) {
            SubMethodInvoke invoke = (SubMethodInvoke) code;
            TestMethod testMethod = getTestMethodByKey(invoke.getSubMethodKey());
            invoke.setSubMethod(testMethod);
            resolveKeyReferenceInCode(invoke.getThisInstance());
            for (Code arg : invoke.getArgs()) {
                resolveKeyReferenceInCode(arg);
            }
        } else if (code instanceof Field) {
            Field field = (Field) code;
            TestField testField = getTestFieldByKey(field.getFieldKey());
            field.setField(testField);
            resolveKeyReferenceInCode(field.getThisInstance());
        } else if (code instanceof VarAssign) {
            VarAssign assign = (VarAssign) code;
            resolveKeyReferenceInCode(assign.getVariable());
            resolveKeyReferenceInCode(assign.getValue());
        } else if (code instanceof ClassInstance) {
            ClassInstance classInstance = (ClassInstance) code;
            TestClass testClass = getTestClassByKey(classInstance.getTestClassKey());
            classInstance.setTestClass(testClass);
        } else if (code instanceof TestStep) {
            TestStep testStep = (TestStep) code;
            for (CodeLine step : testStep.getStepBody()) {
                resolveKeyReferenceInCode(step.getCode());
            }
        }
    }

    // resolve all methodKey and classKey references.
    // assume all keys have been set
    public void resolveKeyReference() throws IllegalDataStructureException {
        for (TestClass testClass : rootClassTable.getTestClasses()) {
            resolveTestMethod(testClass);
            resolveTestField(testClass);
            resolveDelegateToTestClass(testClass);
        }
        for (TestClass testClass : subClassTable.getTestClasses()) {
            resolveTestMethod(testClass);
            resolveTestField(testClass);
            resolveDelegateToTestClass(testClass);
        }
        for (TestMethod testMethod : rootMethodTable.getTestMethods()) {
            resolveTestClass(testMethod);
            for (CodeLine codeLine : testMethod.getCodeBody()) {
                resolveKeyReferenceInCode(codeLine.getCode());
            }
        }
        for (TestMethod testMethod : subMethodTable.getTestMethods()) {
            resolveTestClass(testMethod);
            for (CodeLine codeLine : testMethod.getCodeBody()) {
                resolveKeyReferenceInCode(codeLine.getCode());
            }
        }
        for (TestField testField : fieldTable.getTestFields()) {
            resolveTestClass(testField);
        }
    }

    // merge src to dest
    private void mergeTestClassTable(TestClassTable srcTable, TestClassTable destTable) {
        for (TestClass srcClass : srcTable.getTestClasses()) {
            TestClass destClass = destTable.getByKey(srcClass.getKey());
            if (destClass == null) {
                destTable.addTestClass(srcClass);
            } else {
                // some test methods or test fields may not be included in the test class
                for (String srcMethodKey : srcClass.getTestMethodKeys()) {
                    if (!destClass.getTestMethodKeys().contains(srcMethodKey)) {
                        destClass.addTestMethodKey(srcMethodKey);
                    }
                }
                for (String srcFieldKey : srcClass.getTestFieldKeys()) {
                    if (!destClass.getTestFieldKeys().contains(srcFieldKey)) {
                        destClass.addTestFieldKey(srcFieldKey);
                    }
                }
            }
        }
    }

    // merge src to dest
    private void mergeTestMethodTable(TestMethodTable srcTable, TestMethodTable destTable) {
        for (TestMethod srcMethod : srcTable.getTestMethods()) {
            if (destTable.getByKey(srcMethod.getKey()) == null) {
                destTable.addTestMethod(srcMethod);
            }
        }
    }

    // merge src to dest
    private void mergeTestFieldTable(TestFieldTable srcTable, TestFieldTable destTable) {
        for (TestField srcField : srcTable.getTestFields()) {
            if (destTable.getByKey(srcField.getKey()) == null) {
                destTable.addTestField(srcField);
            }
        }
    }

    // if the keys for this tree or the specified srcTree have already been resolved,
    // you should call resolveKeyReference again for this tree after calling this method.
    // (since merging can break correct reference)
    public void merge(SrcTree srcTree) {
        mergeTestClassTable(srcTree.rootClassTable, rootClassTable);
        mergeTestClassTable(srcTree.subClassTable, subClassTable);
        mergeTestMethodTable(srcTree.rootMethodTable, rootMethodTable);
        mergeTestMethodTable(srcTree.subMethodTable, subMethodTable);
        mergeTestFieldTable(srcTree.fieldTable, fieldTable);
    }
}
