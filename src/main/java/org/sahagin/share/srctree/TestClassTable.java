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

//class or interface
public class TestClassTable implements YamlConvertible {
    private List<TestClass> testClasses = new ArrayList<TestClass>(512);

    public List<TestClass> getTestClasses() {
        return testClasses;
    }

    public void addTestClass(TestClass testClass) {
        testClasses.add(testClass);
    }

    public boolean isEmpty() {
        return testClasses.isEmpty();
    }

    // returns null if not found
    public TestClass getByKey(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        for (TestClass testClass : testClasses) {
            if (key.equals(testClass.getKey())) {
                return testClass;
            }
        }
        return null;
    }

    public void sort() {
        Collections.sort(testClasses, new Comparator<TestClass>() {

            // sort by name as much as possible
            // since sometimes key differs between Windows and OSX
            @Override
            public int compare(TestClass left, TestClass right) {
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
        if (!isEmpty()) {
            result.put("classes", YamlUtils.toYamlObjectList(testClasses));
        }
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        List<Map<String, Object>> testClassesYamlObj
        = YamlUtils.getYamlObjectListValue(yamlObject, "classes", true);
        testClasses = new ArrayList<TestClass>(testClassesYamlObj.size());
        for (Map<String, Object> testClassYamlObj : testClassesYamlObj) {
            TestClass testClass = TestClass.newInstanceFromYamlObject(testClassYamlObj);
            testClasses.add(testClass);
        }
    }

}
