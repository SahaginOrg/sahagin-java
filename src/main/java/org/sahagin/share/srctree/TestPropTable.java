package org.sahagin.share.srctree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sahagin.share.CommonUtils;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlConvertible;
import org.sahagin.share.yaml.YamlUtils;

public class TestPropTable implements YamlConvertible {
    private List<TestProp> testProps = new ArrayList<TestProp>(512);

    public List<TestProp> getTestProps() {
        return testProps;
    }

    public void addTestProp(TestProp testProp) {
        testProps.add(testProp);
    }

    public boolean isEmpty() {
        return testProps.isEmpty();
    }

    // returns null if not found
    public TestProp getByKey(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        for (TestProp testProp : testProps) {
            if (key.equals(testProp.getKey())) {
                return testProp;
            }
        }
        return null;
    }

    // returns null if not found
    public List<TestProp> getByName(String classQualifiedName, String propSimpleName) {
        if (classQualifiedName == null) {
            throw new NullPointerException();
        }
        if (propSimpleName == null) {
            throw new NullPointerException();
        }
        List<TestProp> result = new ArrayList<TestProp>(1);
        for (TestProp testProp : testProps) {
            if (StringUtils.equals(classQualifiedName, testProp.getTestClass().getQualifiedName())
                    && StringUtils.equals(propSimpleName, testProp.getSimpleName())) {
                result.add(testProp);
            }
        }
        return result;
    }

    public void sort() {
        Collections.sort(testProps, new Comparator<TestProp>() {

            // sort by name as much as possible
            // since sometimes key differs between Windows and OSX
            @Override
            public int compare(TestProp left, TestProp right) {
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
            result.put("props", YamlUtils.toYamlObjectList(testProps));
        }
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        List<Map<String, Object>> testPropsYamlObj
        = YamlUtils.getYamlObjectListValue(yamlObject, "props", true);
        testProps = new ArrayList<TestProp>(testPropsYamlObj.size());
        for (Map<String, Object> testPropYamlObj : testPropsYamlObj) {
            TestProp testProp = new TestProp();
            testProp.fromYamlObject(testPropYamlObj);
            testProps.add(testProp);
        }
    }

}
