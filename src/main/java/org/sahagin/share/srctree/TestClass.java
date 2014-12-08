package org.sahagin.share.srctree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sahagin.share.yaml.YamlUtils;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlConvertible;

public class TestClass implements YamlConvertible {
    public static final String MSG_INVALID_TYPE = "invalid type: %s";
    public static final String TYPE = "class";

    private String key;
    private String qualifiedName;
    private String testDoc;
    private List<String> testMethodKeys = new ArrayList<String>(16);
    private List<TestMethod> testMethods = new ArrayList<TestMethod>(16);

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSimpleName() {
        if (qualifiedName == null) {
            return null;
        }
        int lastIndex = qualifiedName.lastIndexOf("."); // TODO name separator is always dot ??
        if (lastIndex == -1) {
            return qualifiedName;
        }
        return qualifiedName.substring(lastIndex + 1);
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public String getTestDoc() {
        return testDoc;
    }

    public void setTestDoc(String testDoc) {
        this.testDoc = testDoc;
    }

    public List<String> getTestMethodKeys() {
        return testMethodKeys;
    }

    public void addTestMethodKey(String testMethodKey) {
        this.testMethodKeys.add(testMethodKey);
    }

    public List<TestMethod> getTestMethods() {
        return testMethods;
    }

    public void addTestMethod(TestMethod testMethod) {
        testMethods.add(testMethod);
    }

    public void clearTestMethods() {
        testMethods.clear();
    }

    protected String getType() {
        return TYPE;
    }

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = new HashMap<String, Object>(8);
        result.put("type", getType());
        result.put("key", key);
        result.put("name", qualifiedName);
        result.put("testDoc", testDoc);
        result.put("methodKeys", testMethodKeys);
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        YamlUtils.strValueEqualsCheck(yamlObject, "type", getType());
        key = YamlUtils.getStrValue(yamlObject, "key");
        qualifiedName = YamlUtils.getStrValue(yamlObject, "name");
        testDoc = YamlUtils.getStrValue(yamlObject, "testDoc");
        testMethodKeys = YamlUtils.getStrListValue(yamlObject, "methodKeys");
        testMethods.clear();
    }

    public static TestClass newInstanceFromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        String type = YamlUtils.getStrValue(yamlObject, "type");
        TestClass result;
        if (TestClass.TYPE.equals(type)) {
            result = new TestClass();
        } else if (PageClass.TYPE.equals(type)) {
            result = new PageClass();
        } else {
            throw new YamlConvertException(String.format(MSG_INVALID_TYPE, type));
        }
        result.fromYamlObject(yamlObject);
        return result;
    }

}
