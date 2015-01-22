package org.sahagin.share.srctree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sahagin.share.CommonUtils;
import org.sahagin.share.yaml.YamlUtils;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlConvertible;

public class TestMethodTable implements YamlConvertible {
    private List<TestMethod> testMethods = new ArrayList<TestMethod>(512);

    public List<TestMethod> getTestMethods() {
        return testMethods;
    }

    public void addTestMethod(TestMethod testMethod) {
        testMethods.add(testMethod);
    }

    // returns null if not found
    public TestMethod getByKey(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        for (TestMethod testMethod : testMethods) {
            if (key.equals(testMethod.getKey())) {
                return testMethod;
            }
        }
        return null;
    }

    public List<TestMethod> getByName(String classQualifiedName, String methodSimpleName) {
        if (classQualifiedName == null) {
            throw new NullPointerException();
        }
        if (methodSimpleName == null) {
            throw new NullPointerException();
        }
        List<TestMethod> result = new ArrayList<TestMethod>(1);
        for (TestMethod testMethod : testMethods) {
            if (StringUtils.equals(classQualifiedName, testMethod.getTestClass().getQualifiedName())
                    && StringUtils.equals(methodSimpleName, testMethod.getSimpleName())) {
                result.add(testMethod);
            }
        }
        return result;
    }

    public void sort() {
        Collections.sort(testMethods, new Comparator<TestMethod>() {

            // sort by name as much as possible
            // since sometimes key differs between Windows and OSX
            @Override
            public int compare(TestMethod left, TestMethod right) {
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
        result.put("methods", YamlUtils.toYamlObjectList(testMethods));
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        List<Map<String, Object>> testMethodsYamlObj = YamlUtils.getYamlObjectListValue(yamlObject, "methods");
        testMethods = new ArrayList<TestMethod>(testMethodsYamlObj.size());
        for (Map<String, Object> testMethodYamlObj : testMethodsYamlObj) {
            TestMethod testMethod = new TestMethod();
            testMethod.fromYamlObject(testMethodYamlObj);
            testMethods.add(testMethod);
        }
    }

}
