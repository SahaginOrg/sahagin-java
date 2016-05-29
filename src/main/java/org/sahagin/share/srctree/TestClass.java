package org.sahagin.share.srctree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sahagin.share.yaml.YamlUtils;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlConvertible;

// class or interface
public class TestClass extends ASTData implements YamlConvertible {
    public static final String MSG_INVALID_TYPE = "invalid type: %s";
    public static final String TYPE = "class";
    private static final String DEFAULT_TYPE = TYPE;

    private String key;
    private String qualifiedName;
    private String testDoc;
    // null represents no delegation
    private String delegateToTestClassKey = null;
    private TestClass delegateToTestClass = null;
    private List<String> testMethodKeys = new ArrayList<>(16);
    private List<TestMethod> testMethods = new ArrayList<>(16);
    private List<String> testFieldKeys = new ArrayList<>(16);
    private List<TestField> testFields = new ArrayList<>(16);

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
        int lastIndex = qualifiedName.lastIndexOf(".");
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

    public String getDelegateToTestClassKey() {
        return delegateToTestClassKey;
    }

    public void setDelegateToTestClassKey(String delegateToTestClassKey) {
        this.delegateToTestClassKey = delegateToTestClassKey;
    }

    public TestClass getDelegateToTestClass() {
        return delegateToTestClass;
    }

    public void setDelegateToTestClass(TestClass delegateToTestClass) {
        this.delegateToTestClass = delegateToTestClass;
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

    public List<String> getTestFieldKeys() {
        return testFieldKeys;
    }

    public void addTestFieldKey(String testFieldKey) {
        testFieldKeys.add(testFieldKey);
    }

    public List<TestField> getTestFields() {
        return testFields;
    }

    public void addTestField(TestField testField) {
        testFields.add(testField);
    }

    public void clearTestFields() {
        testFields.clear();
    }

    protected String getType() {
        return TYPE;
    }

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = new HashMap<>(8);
        result.put("key", key);
        result.put("qname", qualifiedName);
        if (!getType().equals(DEFAULT_TYPE)) {
            result.put("type", getType());
        }
        if (testDoc != null) {
            result.put("testDoc", testDoc);
        }
        if (delegateToTestClassKey != null) {
            result.put("delegateToClassKey", delegateToTestClassKey);
        }
        if (!testMethodKeys.isEmpty()) {
            result.put("methodKeys", testMethodKeys);
        }
        if (!testFieldKeys.isEmpty()) {
            result.put("fieldKeys", testFieldKeys);
        }
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        YamlUtils.strValueEqualsCheck(yamlObject, "type", getType(), DEFAULT_TYPE);
        key = YamlUtils.getStrValue(yamlObject, "key");
        qualifiedName = YamlUtils.getStrValue(yamlObject, "qname");
        testDoc = YamlUtils.getStrValue(yamlObject, "testDoc", true);
        delegateToTestClassKey = YamlUtils.getStrValue(yamlObject, "delegateToClassKey", true);
        delegateToTestClass = null;
        testMethodKeys = YamlUtils.getStrListValue(yamlObject, "methodKeys", true);
        testMethods.clear();
        testFieldKeys = YamlUtils.getStrListValue(yamlObject, "fieldKeys", true);
        testFields.clear();
    }

    public static TestClass newInstanceFromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        String type = YamlUtils.getStrValue(yamlObject, "type", true);
        if (type == null) {
            type = DEFAULT_TYPE;
        }
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
