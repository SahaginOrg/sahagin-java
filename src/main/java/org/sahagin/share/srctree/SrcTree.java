package org.sahagin.share.srctree;

import java.util.HashMap;
import java.util.Map;

import org.sahagin.share.IllegalDataStructureException;
import org.sahagin.share.srctree.code.Code;
import org.sahagin.share.srctree.code.CodeLine;
import org.sahagin.share.srctree.code.SubFunctionInvoke;
import org.sahagin.share.srctree.code.SubMethodInvoke;
import org.sahagin.share.yaml.YamlUtils;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlConvertible;

public class SrcTree implements YamlConvertible {
    private static final String MSG_CLASS_NOT_FOUND = "class not found; key: %s";
    private static final String MSG_FUNCTION_NOT_FOUND = "function not found; key: %s";
    private static final String MSG_NOT_METHOD = "function \"%s\" is not a method";

    private TestClassTable rootClassTable;
    private TestFuncTable rootFuncTable;
    private TestClassTable subClassTable;
    private TestFuncTable subFuncTable;

    public TestClassTable getRootClassTable() {
        return rootClassTable;
    }

    public void setRootClassTable(TestClassTable rootClassTable) {
        this.rootClassTable = rootClassTable;
    }

    public TestFuncTable getRootFuncTable() {
        return rootFuncTable;
    }

    public void setRootFuncTable(TestFuncTable rootFuncTable) {
        this.rootFuncTable = rootFuncTable;
    }

    public TestClassTable getSubClassTable() {
        return subClassTable;
    }

    public void setSubClassTable(TestClassTable subClassTable) {
        this.subClassTable = subClassTable;
    }

    public TestFuncTable getSubFuncTable() {
        return subFuncTable;
    }

    public void setSubFuncTable(TestFuncTable subFuncTable) {
        this.subFuncTable = subFuncTable;
    }

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = new HashMap<String, Object>(4);

        Map<String, Object> rootClassTableYamlObj = null;
        if (rootClassTable != null) {
            rootClassTableYamlObj = rootClassTable.toYamlObject();
        }
        Map<String, Object> rootFuncTableYamlObj = null;
        if (rootFuncTable != null) {
            rootFuncTableYamlObj = rootFuncTable.toYamlObject();
        }
        Map<String, Object> subClassTableYamlObj = null;
        if (subClassTable != null) {
            subClassTableYamlObj = subClassTable.toYamlObject();
        }
        Map<String, Object> subFuncTableYamlObj = null;
        if (subFuncTable != null) {
            subFuncTableYamlObj = subFuncTable.toYamlObject();
        }

        result.put("rootClassTable", rootClassTableYamlObj);
        result.put("rootFuncTable", rootFuncTableYamlObj);
        result.put("subClassTable", subClassTableYamlObj);
        result.put("subFuncTable", subFuncTableYamlObj);
        result.put("sahagin-version", "0.2.1"); // TODO hard coded..

        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        rootClassTable = null;
        rootFuncTable = null;
        subClassTable = null;
        subFuncTable = null;

        Map<String, Object> rootClassTableYamlObj = YamlUtils.getYamlObjectValue(yamlObject, "rootClassTable");
        if (rootClassTableYamlObj != null) {
            rootClassTable = new TestClassTable();
            rootClassTable.fromYamlObject(rootClassTableYamlObj);
        }
        Map<String, Object> rootFuncTableYamlObj = YamlUtils.getYamlObjectValue(yamlObject, "rootFuncTable");
        if (rootFuncTableYamlObj != null) {
            rootFuncTable = new TestFuncTable();
            rootFuncTable.fromYamlObject(rootFuncTableYamlObj);
        }
        Map<String, Object> subClassTableYamlObj = YamlUtils.getYamlObjectValue(yamlObject, "subClassTable");
        if (subClassTableYamlObj != null) {
            subClassTable = new TestClassTable();
            subClassTable.fromYamlObject(subClassTableYamlObj);
        }
        Map<String, Object> subFuncTableYamlObj = YamlUtils.getYamlObjectValue(yamlObject, "subFuncTable");
        if (subFuncTableYamlObj != null) {
            subFuncTable = new TestFuncTable();
            subFuncTable.fromYamlObject(subFuncTableYamlObj);
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

    public TestFunction getTestFunctionByKey(String testFunctionKey) throws IllegalDataStructureException {
        if (subFuncTable != null) {
            TestFunction subFunc = subFuncTable.getByKey(testFunctionKey);
            if (subFunc != null) {
                return subFunc;
            }
        }
        if (rootFuncTable != null) {
            TestFunction rootFunc = rootFuncTable.getByKey(testFunctionKey);
            if (rootFunc != null) {
                return rootFunc;
            }
        }
        throw new IllegalDataStructureException(String.format(MSG_FUNCTION_NOT_FOUND, testFunctionKey));
    }

    private TestMethod getTestMethodByKey(String testMethodKey) throws IllegalDataStructureException {
        TestFunction testFunction = getTestFunctionByKey(testMethodKey);
        if (!(testFunction instanceof TestMethod)) {
            throw new IllegalDataStructureException(String.format(MSG_NOT_METHOD,
                    testFunction.getQualifiedName()));
        }
        return (TestMethod) testFunction;
    }

    private void resolveTestClass(TestFunction testFunction) throws IllegalDataStructureException {
        if (!(testFunction instanceof TestMethod)) {
            return;
        }
        TestMethod testMethod = (TestMethod) testFunction;
        testMethod.setTestClass(getTestClassByKey(testMethod.getTestClassKey()));
    }

    private void resolveTestMethod(TestClass testClass) throws IllegalDataStructureException {
        testClass.clearTestMethods();
        for (String testMethodKey : testClass.getTestMethodKeys()) {
            TestMethod testMethod = getTestMethodByKey(testMethodKey);
            testClass.addTestMethod(testMethod);
        }
    }

    private void resolveTestFunction(Code code) throws IllegalDataStructureException {
        if (code instanceof SubMethodInvoke) {
            SubMethodInvoke invoke = (SubMethodInvoke) code;
            TestMethod testMethod = getTestMethodByKey(invoke.getSubMethodKey());
            invoke.setSubMethod(testMethod);
            resolveTestFunction(invoke.getThisInstance());
            for (Code arg : invoke.getArgs()) {
                resolveTestFunction(arg);
            }
        } else if (code instanceof SubFunctionInvoke) {
            SubFunctionInvoke invoke = (SubFunctionInvoke) code;
            TestFunction testFunction = getTestFunctionByKey(invoke.getSubFunctionKey());
            invoke.setSubFunction(testFunction);
            for (Code arg : invoke.getArgs()) {
                resolveTestFunction(arg);
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
        if (rootFuncTable != null) {
            for (TestFunction testFunction : rootFuncTable.getTestFunctions()) {
                resolveTestClass(testFunction);
                for (CodeLine codeLine : testFunction.getCodeBody()) {
                    resolveTestFunction(codeLine.getCode());
                }
            }
        }
        if (subFuncTable != null) {
            for (TestFunction testFunction : subFuncTable.getTestFunctions()) {
                resolveTestClass(testFunction);
                for (CodeLine codeLine : testFunction.getCodeBody()) {
                    resolveTestFunction(codeLine.getCode());
                }
            }
        }
    }

}
