package org.sahagin.share.srctree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sahagin.share.CommonUtils;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlConvertible;
import org.sahagin.share.yaml.YamlUtils;

public class TestFieldTable implements YamlConvertible {
    private List<TestField> testFields = new ArrayList<TestField>(512);

    public List<TestField> getTestFields() {
        return testFields;
    }

    public void addTestField(TestField testField) {
        testFields.add(testField);
    }

    public boolean isEmpty() {
        return testFields.isEmpty();
    }

    // returns null if not found
    public TestField getByKey(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        for (TestField testField : testFields) {
            if (key.equals(testField.getKey())) {
                return testField;
            }
        }
        return null;
    }

    public void sort() {
        Collections.sort(testFields, new Comparator<TestField>() {

            // sort by name as much as possible
            // since sometimes key differs between Windows and OSX
            @Override
            public int compare(TestField left, TestField right) {
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
            result.put("fields", YamlUtils.toYamlObjectList(testFields));
        }
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        List<Map<String, Object>> testFieldsYamlObj
        = YamlUtils.getYamlObjectListValue(yamlObject, "fields", true);
        testFields = new ArrayList<TestField>(testFieldsYamlObj.size());
        for (Map<String, Object> testFieldYamlObj : testFieldsYamlObj) {
            TestField testField = new TestField();
            testField.fromYamlObject(testFieldYamlObj);
            testFields.add(testField);
        }
    }

}
