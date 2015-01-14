package org.sahagin.share.srctree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sahagin.share.CommonUtils;
import org.sahagin.share.yaml.YamlUtils;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlConvertible;

public class TestFuncTable implements YamlConvertible {
    private List<TestFunction> testFunctions = new ArrayList<TestFunction>(512);

    public List<TestFunction> getTestFunctions() {
        return testFunctions;
    }

    public void addTestFunction(TestFunction testFunction) {
        testFunctions.add(testFunction);
    }

    // returns null if not found
    public TestFunction getByKey(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        for (TestFunction testFunction : testFunctions) {
            if (key.equals(testFunction.getKey())) {
                return testFunction;
            }
        }
        return null;
    }

    public List<TestFunction> getByQualifiedName(String qualifiedName) {
        if (qualifiedName == null) {
            throw new NullPointerException();
        }
        List<TestFunction> result = new ArrayList<TestFunction>(1);
        for (TestFunction testFunction : testFunctions) {
            if (qualifiedName.equals(testFunction.getQualifiedName())) {
                result.add(testFunction);
            }
        }
        return result;
    }

    public void sort() {
        Collections.sort(testFunctions, new Comparator<TestFunction>() {

            // sort by name as much as possible
            // since sometimes key differs between Windows and OSX
            @Override
            public int compare(TestFunction left, TestFunction right) {
                int nameCompareResult = CommonUtils.compare(left.getQualifiedName(), right.getQualifiedName());
                if (nameCompareResult == 0) {
                    return CommonUtils.compare(left.getKey(), right.getKey());
                } else {
                    return nameCompareResult;
                }
            }
        });
    }

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = new HashMap<String, Object>(1);
        result.put("functions", YamlUtils.toYamlObjectList(testFunctions));
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        List<Map<String, Object>> testFunctionsYamlObj = YamlUtils.getYamlObjectListValue(yamlObject, "functions");
        testFunctions = new ArrayList<TestFunction>(testFunctionsYamlObj.size());
        for (Map<String, Object> testFunctionYamlObj : testFunctionsYamlObj) {
            TestFunction testFunction = TestFunction.newInstanceFromYamlObject(testFunctionYamlObj);
            testFunctions.add(testFunction);
        }
    }

}
