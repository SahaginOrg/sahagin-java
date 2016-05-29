package org.sahagin.share.srctree.code;

import java.util.Map;

import org.sahagin.share.srctree.TestClass;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlUtils;

public class ClassInstance extends Code {
    public static final String TYPE = "classInstance";
    private String testClassKey;
    private TestClass testClass;

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

    @Override
    protected String getType() {
        return TYPE;
    }

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = super.toYamlObject();
        result.put("classKey", testClassKey);
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        super.fromYamlObject(yamlObject);
        testClassKey = YamlUtils.getStrValue(yamlObject, "classKey");
    }
}
