package org.sahagin.share.srctree.code;

import java.util.Map;

import org.sahagin.share.srctree.TestFunction;
import org.sahagin.share.srctree.TestMethod;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlUtils;

public class SubMethodInvoke extends SubFunctionInvoke {
    public static final String TYPE = "method";
    private Code thisInstance;

    public String getSubMethodKey() {
        return getSubFunctionKey();
    }

    public void setSubMethodKey(String subMethodKey) {
        setSubFunctionKey(subMethodKey);
    }

    public TestMethod getSubMethod() {
        return (TestMethod)getSubFunction();
    }

    @Override
    public void setSubFunction(TestFunction subFunction) {
        if (!(subFunction instanceof TestMethod)) {
            throw new IllegalArgumentException("not testMethod: " + subFunction);
        }
        super.setSubFunction(subFunction);
    }

    public void setSubMethod(TestMethod subMethod) {
        setSubFunction(subMethod);
    }

    @Override
    protected String getType() {
        return TYPE;
    }

    @Override
    protected String getFunctionKeyName() {
        return "methodKey";
    }

    public Code getThisInstance() {
        return thisInstance;
    }

    public void setThisInstance(Code thisInstance) {
        this.thisInstance = thisInstance;
    }
    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = super.toYamlObject();
        if (thisInstance != null) {
            result.put("thisInstance", thisInstance.toYamlObject());
        }
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        super.fromYamlObject(yamlObject);
        Map<String, Object> thisInstanceYamlObj = YamlUtils.getYamlObjectValue(yamlObject, "thisInstance", true);
        if (thisInstanceYamlObj == null) {
            thisInstance = null;
        } else {
            thisInstance = Code.newInstanceFromYamlObject(thisInstanceYamlObj);
        }
    }

}
