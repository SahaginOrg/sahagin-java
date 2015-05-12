package org.sahagin.share.srctree.code;

import java.util.HashMap;
import java.util.Map;

import org.sahagin.share.srctree.ASTData;
import org.sahagin.share.yaml.YamlUtils;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlConvertible;

public abstract class Code extends ASTData implements YamlConvertible {
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
        clearMemo();
    }

    public static Code newInstanceFromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        if (yamlObject == null) {
            return null;
        }
        String type = YamlUtils.getStrValue(yamlObject, "type");
        Code result;
        if (StringCode.TYPE.equals(type)) {
            result = new StringCode();
        } else if (MethodArgument.TYPE.equals(type)) {
            result = new MethodArgument();
        } else if (SubMethodInvoke.TYPE.equals(type)) {
            result = new SubMethodInvoke();
        } else if (Field.TYPE.equals(type)) {
            result = new Field();
        } else if (LocalVar.TYPE.equals(type)) {
            result = new LocalVar();
        } else if (VarAssign.TYPE.equals(type)) {
            result = new VarAssign();
        } else if (ClassInstance.TYPE.equals(type)) {
            result = new ClassInstance();
        } else if (TestStep.TYPE.equals(type)) {
            result = new TestStep();
        } else if (TestStepLabel.TYPE.equals(type)) {
            result = new TestStepLabel();
        } else if (UnknownCode.TYPE.equals(type)) {
            result = new UnknownCode();
        } else {
            throw new YamlConvertException(String.format(MSG_INVALID_TYPE, type));
        }
        result.fromYamlObject(yamlObject);
        return result;
    }

}
