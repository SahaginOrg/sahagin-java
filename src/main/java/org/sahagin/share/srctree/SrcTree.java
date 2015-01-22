package org.sahagin.share.srctree;

import java.util.HashMap;
import java.util.Map;

import org.sahagin.share.CommonUtils;
import org.sahagin.share.IllegalDataStructureException;
import org.sahagin.share.srctree.code.Code;
import org.sahagin.share.srctree.code.CodeLine;
import org.sahagin.share.srctree.code.SubMethodInvoke;
import org.sahagin.share.yaml.YamlUtils;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlConvertible;

public class SrcTree implements YamlConvertible {
    private static final String MSG_CLASS_NOT_FOUND = "class not found; key: %s";
    private static final String MSG_METHOD_NOT_FOUND = "method not found; key: %s";
    private static final String MSG_SRC_TREE_FORMAT_MISMATCH
    = "expected formatVersion is \"%s\", but actual is \"%s\"";

    private TestClassTable rootClassTable;
    private TestMethodTable rootMethodTable;
    private TestClassTable subClassTable;
    private TestMethodTable subMethodTable;

    public TestClassTable getRootClassTable() {
        return rootClassTable;
    }

    public void setRootClassTable(TestClassTable rootClassTable) {
        this.rootClassTable = rootClassTable;
    }

    public TestMethodTable getRootMethodTable() {
        return rootMethodTable;
    }

    public void setRootMethodTable(TestMethodTable rootMethodTable) {
        this.rootMethodTable = rootMethodTable;
    }

    public TestClassTable getSubClassTable() {
        return subClassTable;
    }

    public void setSubClassTable(TestClassTable subClassTable) {
        this.subClassTable = subClassTable;
    }

    public TestMethodTable getSubMethodTable() {
        return subMethodTable;
    }

    public void setSubMethodTable(TestMethodTable subMethodTable) {
        this.subMethodTable = subMethodTable;
    }

    public void sort() {
        rootClassTable.sort();
        rootMethodTable.sort();
        subClassTable.sort();
        subMethodTable.sort();
    }

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = new HashMap<String, Object>(4);

        Map<String, Object> rootClassTableYamlObj = null;
        if (rootClassTable != null) {
            rootClassTableYamlObj = rootClassTable.toYamlObject();
        }
        Map<String, Object> rootMethodTableYamlObj = null;
        if (rootMethodTable != null) {
            rootMethodTableYamlObj = rootMethodTable.toYamlObject();
        }
        Map<String, Object> subClassTableYamlObj = null;
        if (subClassTable != null) {
            subClassTableYamlObj = subClassTable.toYamlObject();
        }
        Map<String, Object> subMethodTableYamlObj = null;
        if (subMethodTable != null) {
            subMethodTableYamlObj = subMethodTable.toYamlObject();
        }

        result.put("rootClassTable", rootClassTableYamlObj);
        result.put("rootMethodTable", rootMethodTableYamlObj);
        result.put("subClassTable", subClassTableYamlObj);
        result.put("subMethodTable", subMethodTableYamlObj);
        result.put("formatVersion", CommonUtils.formatVersion());

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

        rootClassTable = null;
        rootMethodTable = null;
        subClassTable = null;
        subMethodTable = null;

        Map<String, Object> rootClassTableYamlObj = YamlUtils.getYamlObjectValue(yamlObject, "rootClassTable");
        if (rootClassTableYamlObj != null) {
            rootClassTable = new TestClassTable();
            rootClassTable.fromYamlObject(rootClassTableYamlObj);
        }
        Map<String, Object> rootMethodTableYamlObj = YamlUtils.getYamlObjectValue(yamlObject, "rootMethodTable");
        if (rootMethodTableYamlObj != null) {
            rootMethodTable = new TestMethodTable();
            rootMethodTable.fromYamlObject(rootMethodTableYamlObj);
        }
        Map<String, Object> subClassTableYamlObj = YamlUtils.getYamlObjectValue(yamlObject, "subClassTable");
        if (subClassTableYamlObj != null) {
            subClassTable = new TestClassTable();
            subClassTable.fromYamlObject(subClassTableYamlObj);
        }
        Map<String, Object> subMethodTableYamlObj = YamlUtils.getYamlObjectValue(yamlObject, "subMethodTable");
        if (subMethodTableYamlObj != null) {
            subMethodTable = new TestMethodTable();
            subMethodTable.fromYamlObject(subMethodTableYamlObj);
        }
    }

    public TestClass getTestClassByKey(String testClassKey) throws IllegalDataStructureException {
        if (subClassTable != null) {
            TestClass subClass = subClassTable.getByKey(testClassKey);
            if (subClass != null) {
                return subClass;
            }
        }
        if (rootClassTable != null) {
            TestClass rootClass = rootClassTable.getByKey(testClassKey);
            if (rootClass != null) {
                return rootClass;
            }
        }
        throw new IllegalDataStructureException(String.format(MSG_CLASS_NOT_FOUND, testClassKey));
    }

    public TestMethod getTestMethodByKey(String testMethodKey)
            throws IllegalDataStructureException {
        if (subMethodTable != null) {
            TestMethod subMethod = subMethodTable.getByKey(testMethodKey);
            if (subMethod != null) {
                return subMethod;
            }
        }
        if (rootMethodTable != null) {
            TestMethod rootMethod = rootMethodTable.getByKey(testMethodKey);
            if (rootMethod != null) {
                return rootMethod;
            }
        }
        throw new IllegalDataStructureException(String.format(MSG_METHOD_NOT_FOUND, testMethodKey));
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

    private void resolveTestMethod(Code code) throws IllegalDataStructureException {
        if (code instanceof SubMethodInvoke) {
            SubMethodInvoke invoke = (SubMethodInvoke) code;
            TestMethod testMethod = getTestMethodByKey(invoke.getSubMethodKey());
            invoke.setSubMethod(testMethod);
            resolveTestMethod(invoke.getThisInstance());
            for (Code arg : invoke.getArgs()) {
                resolveTestMethod(arg);
            }
        }
    }

    // resolve all methodKey and classKey references.
    // assume all keys have been set
    public void resolveKeyReference() throws IllegalDataStructureException {
        if (rootClassTable != null) {
            for (TestClass testClass : rootClassTable.getTestClasses()) {
                resolveTestMethod(testClass);
            }
        }
        if (subClassTable != null) {
            for (TestClass testClass : subClassTable.getTestClasses()) {
                resolveTestMethod(testClass);
            }
        }
        if (rootMethodTable != null) {
            for (TestMethod testMethod : rootMethodTable.getTestMethods()) {
                resolveTestClass(testMethod);
                for (CodeLine codeLine : testMethod.getCodeBody()) {
                    resolveTestMethod(codeLine.getCode());
                }
            }
        }
        if (subMethodTable != null) {
            for (TestMethod testMethod : subMethodTable.getTestMethods()) {
                resolveTestClass(testMethod);
                for (CodeLine codeLine : testMethod.getCodeBody()) {
                    resolveTestMethod(codeLine.getCode());
                }
            }
        }
    }

}
