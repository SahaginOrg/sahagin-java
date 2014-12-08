package org.sahagin.share.srctree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sahagin.share.yaml.YamlUtils;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlConvertible;

public class TestClassTable implements YamlConvertible {
    private List<TestClass> testClasses = new ArrayList<TestClass>(512);

    public List<TestClass> getTestClasses() {
        return testClasses;
    }

    public void addTestClass(TestClass testClass) {
        testClasses.add(testClass);
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

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = new HashMap<String, Object>(1);
        result.put("classes", YamlUtils.toYamlObjectList(testClasses));
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        List<Map<String, Object>> testClassesYamlObj = YamlUtils.getYamlObjectListValue(yamlObject, "classes");
        testClasses = new ArrayList<TestClass>(testClassesYamlObj.size());
        for (Map<String, Object> testClassYamlObj : testClassesYamlObj) {
            TestClass testClass = TestClass.newInstanceFromYamlObject(testClassYamlObj);
            testClasses.add(testClass);
        }
    }

}
