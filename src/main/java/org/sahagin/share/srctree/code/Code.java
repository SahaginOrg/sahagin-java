package org.sahagin.share.srctree.code;

import java.util.HashMap;
import java.util.Map;

import org.sahagin.share.yaml.YamlUtils;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlConvertible;

public abstract class Code implements YamlConvertible {
    public static final String MSG_INVALID_TYPE = "invalid type: %s";

    private String original;

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    protected abstract String getType();

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = new HashMap<String, Object>(8);
        result.put("type", getType());
        result.put("original", original);
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject) throws YamlConvertException {
        YamlUtils.strValueEqualsCheck(yamlObject, "type", getType());
        original = YamlUtils.getStrValue(yamlObject, "original");
    }

    public static Code newInstanceFromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        String type = YamlUtils.getStrValue(yamlObject, "type");
        Code result;
        if (StringCode.TYPE.equals(type)) {
            result = new StringCode();
        } else if (MethodArgument.TYPE.equals(type)) {
            result = new MethodArgument();
        } else if (SubMethodInvoke.TYPE.equals(type)) {
            result = new SubMethodInvoke();
        } else if (LocalVar.TYPE.equals(type)) {
            result = new LocalVar();
        } else if (LocalVarAssign.TYPE.equals(type)) {
            result = new LocalVarAssign();
        } else if (TestStep.TYPE.equals(type)) {
            result = new TestStep();
        } else if (UnknownCode.TYPE.equals(type)) {
            result = new UnknownCode();
        } else {
            throw new YamlConvertException(String.format(MSG_INVALID_TYPE, type));
        }
        result.fromYamlObject(yamlObject);
        return result;
    }

}
