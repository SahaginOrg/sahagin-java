package org.sahagin.share.srctree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sahagin.runlib.external.CaptureStyle;
import org.sahagin.share.srctree.code.Code;
import org.sahagin.share.srctree.code.CodeLine;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlConvertible;
import org.sahagin.share.yaml.YamlUtils;

public class TestProp implements YamlConvertible {
    private String testClassKey;
    private TestClass testClass;
    private String key;
    private String simpleName;
    private String testDoc;
    private Code value;

    public String getTestClassKey() {
        return testClassKey;
    }

    public void setTestClassKey(String testClassKey) {
        this.testClassKey = testClassKey;
    }

    public TestClass getTestClass() {
        return testClass;
    }

    public void setTestClass(TestClass testClass) {
        this.testClass = testClass;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    public String getQualifiedName() {
        if (testClass == null || simpleName == null) {
            return simpleName;
        } else {
            return testClass.getQualifiedName() + "." + simpleName;
        }
    }

    public String getTestDoc() {
        return testDoc;
    }

    public void setTestDoc(String testDoc) {
        this.testDoc = testDoc;
    }

    public Code getValue() {
        return value;
    }

    public void setValue(Code value) {
        this.value = value;
    }

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = new HashMap<String, Object>(8);
        result.put("classKey", testClassKey);
        result.put("key", key);
        result.put("name", simpleName);
        if (testDoc != null) {
            result.put("testDoc", testDoc);
        }
        result.put("value", YamlUtils.toYamlObject(value));
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        testClass = null;
        testClassKey = YamlUtils.getStrValue(yamlObject, "classKey");
        key = YamlUtils.getStrValue(yamlObject, "key");
        simpleName = YamlUtils.getStrValue(yamlObject, "name");
        testDoc = YamlUtils.getStrValue(yamlObject, "testDoc", true);
        Map<String, Object> valueYamlObj = YamlUtils.getYamlObjectValue(yamlObject, "value");
        value = Code.newInstanceFromYamlObject(valueYamlObj);
    }

}
