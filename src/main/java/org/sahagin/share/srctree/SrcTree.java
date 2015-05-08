package org.sahagin.share.srctree;

import java.util.HashMap;
import java.util.Map;

import org.sahagin.share.CommonUtils;
import org.sahagin.share.IllegalDataStructureException;
import org.sahagin.share.srctree.code.Code;
import org.sahagin.share.srctree.code.CodeLine;
import org.sahagin.share.srctree.code.LocalVarAssign;
import org.sahagin.share.srctree.code.SubMethodInvoke;
import org.sahagin.share.srctree.code.TestStep;
import org.sahagin.share.yaml.YamlUtils;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlConvertible;

public class SrcTree implements YamlConvertible {
    private static final String MSG_CLASS_NOT_FOUND = "class not found; key: %s";
    private static final String MSG_METHOD_NOT_FOUND = "method not found; key: %s";
    private static final String MSG_PROP_NOT_FOUND = "property not found; key: %s";
    private static final String MSG_SRC_TREE_FORMAT_MISMATCH
    = "expected formatVersion is \"%s\", but actual is \"%s\"";

    private TestClassTable rootClassTable = new TestClassTable();
    private TestMethodTable rootMethodTable = new TestMethodTable();
    private TestPropTable rootPropTable = new TestPropTable();
    private TestClassTable subClassTable = new TestClassTable();
    private TestMethodTable subMethodTable = new TestMethodTable();
    private TestPropTable subPropTable = new TestPropTable();

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

    public TestPropTable getRootPropTable() {
        return rootPropTable;
    }

    public void setRootPropTable(TestPropTable rootPropTable) {
        if (rootPropTable == null) {
            throw new NullPointerException();
        }
        this.rootPropTable = rootPropTable;
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

    public TestPropTable getSubPropTable() {
        return subPropTable;
    }

    public void setSubPropTable(TestPropTable subPropTable) {
        if (subPropTable == null) {
            throw new NullPointerException();
        }
        this.subPropTable = subPropTable;
    }

    public void sort() {
        rootClassTable.sort();
        rootMethodTable.sort();
        rootPropTable.sort();
        subClassTable.sort();
        subMethodTable.sort();
        subPropTable.sort();
    }

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = new HashMap<String, Object>(8);
        result.put("formatVersion", CommonUtils.formatVersion());
        if (!rootPropTable.isEmpty()) {
            result.put("rootPropTable", rootPropTable.toYamlObject());
        }
        if (!subPropTable.isEmpty()) {
            result.put("subPropTable", subPropTable.toYamlObject());
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

        rootPropTable = new TestPropTable();
        Map<String, Object> rootPropTableYamlObj
        = YamlUtils.getYamlObjectValue(yamlObject, "rootPropTable", true);
        if (rootPropTableYamlObj != null) {
            rootPropTable.fromYamlObject(rootPropTableYamlObj);
        }

        subPropTable = new TestPropTable();
        Map<String, Object> subPropTableYamlObj
        = YamlUtils.getYamlObjectValue(yamlObject, "subPropTable", true);
        if (subPropTableYamlObj != null) {
            subPropTable.fromYamlObject(subPropTableYamlObj);
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

    public TestClass getTestClassByKey(String testClassKey) throws IllegalDataStructureException {
        TestClass subClass = subClassTable.getByKey(testClassKey);
        if (subClass != null) {
            return subClass;
        }
        TestClass rootClass = rootClassTable.getByKey(testClassKey);
        if (rootClass != null) {
            return rootClass;
        }
        throw new IllegalDataStructureException(String.format(MSG_CLASS_NOT_FOUND, testClassKey));
    }

    public TestMethod getTestMethodByKey(String testMethodKey)
            throws IllegalDataStructureException {
        TestMethod subMethod = subMethodTable.getByKey(testMethodKey);
        if (subMethod != null) {
            return subMethod;
        }
        TestMethod rootMethod = rootMethodTable.getByKey(testMethodKey);
        if (rootMethod != null) {
            return rootMethod;
        }
        throw new IllegalDataStructureException(String.format(MSG_METHOD_NOT_FOUND, testMethodKey));
    }

    public TestProp getTestPropByKey(String testPropKey)
            throws IllegalDataStructureException {
        TestProp subProp = subPropTable.getByKey(testPropKey);
        if (subProp != null) {
            return subProp;
        }
        TestProp rootProp = rootPropTable.getByKey(testPropKey);
        if (rootProp != null) {
            return rootProp;
        }
        throw new IllegalDataStructureException(String.format(MSG_PROP_NOT_FOUND, testPropKey));
    }

    private void resolveTestClass(TestMethod testMethod) throws IllegalDataStructureException {
        testMethod.setTestClass(getTestClassByKey(testMethod.getTestClassKey()));
    }

    private void resolveTestMethod(TestClass testClass) throws IllegalDataStructureException {
        testClass.clearTestMethods();
        for (String testMethodKey : testClass.getTestMethodKeys()) {
            TestMethod testMethod = getTestMethodByKey(testMethodKey);
            testClass.addTestMethod(testMethod);
        }
    }

    private void resolveTestProp(TestClass testClass) throws IllegalDataStructureException {
        testClass.clearTestProps();
        for (String testPropKey : testClass.getTestPropKeys()) {
            TestProp testProp = getTestPropByKey(testPropKey);
            testClass.addTestProp(testProp);
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

    private void resolveTestMethod(Code code) throws IllegalDataStructureException {
        if (code instanceof SubMethodInvoke) {
            SubMethodInvoke invoke = (SubMethodInvoke) code;
            TestMethod testMethod = getTestMethodByKey(invoke.getSubMethodKey());
            invoke.setSubMethod(testMethod);
            resolveTestMethod(invoke.getThisInstance());
            for (Code arg : invoke.getArgs()) {
                resolveTestMethod(arg);
            }
        } else if (code instanceof LocalVarAssign) {
            LocalVarAssign assign = (LocalVarAssign) code;
            resolveTestMethod(assign.getValue());
        } else if (code instanceof TestStep) {
            TestStep testStep = (TestStep) code;
            for (CodeLine step : testStep.getStepBody()) {
                resolveTestMethod(step.getCode());
            }
        }
    }

    // resolve all methodKey and classKey references.
    // assume all keys have been set
    public void resolveKeyReference() throws IllegalDataStructureException {
        for (TestClass testClass : rootClassTable.getTestClasses()) {
            resolveTestMethod(testClass);
            resolveTestProp(testClass);
            resolveDelegateToTestClass(testClass);
        }
        for (TestClass testClass : subClassTable.getTestClasses()) {
            resolveTestMethod(testClass);
            resolveTestProp(testClass);
            resolveDelegateToTestClass(testClass);
        }
        for (TestMethod testMethod : rootMethodTable.getTestMethods()) {
            resolveTestClass(testMethod);
            for (CodeLine codeLine : testMethod.getCodeBody()) {
                resolveTestMethod(codeLine.getCode());
            }
        }
        for (TestMethod testMethod : subMethodTable.getTestMethods()) {
            resolveTestClass(testMethod);
            for (CodeLine codeLine : testMethod.getCodeBody()) {
                resolveTestMethod(codeLine.getCode());
            }
        }
    }

}
