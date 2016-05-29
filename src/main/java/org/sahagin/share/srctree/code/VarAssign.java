package org.sahagin.share.srctree.code;

import java.util.Map;

import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlUtils;

// Field assignment or LocalVar assignment
public class VarAssign extends Code {
    public static final String TYPE = "varAssign";
    private Code variable;
    private Code value;

    public Code getVariable() {
        return variable;
    }

    public void setVariable(Code variable) {
        this.variable = variable;
    }

    public Code getValue() {
        return value;
    }

    public void setValue(Code value) {
        this.value = value;
    }

    @Override
    protected String getType() {
        return TYPE;
    }

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = super.toYamlObject();
        result.put("var", YamlUtils.toYamlObject(variable));
        result.put("value", YamlUtils.toYamlObject(value));
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        super.fromYamlObject(yamlObject);
        Map<String, Object> varYamlObj = YamlUtils.getYamlObjectValue(yamlObject, "var");
        variable = Code.newInstanceFromYamlObject(varYamlObj);
        Map<String, Object> valueYamlObj = YamlUtils.getYamlObjectValue(yamlObject, "value");
        value = Code.newInstanceFromYamlObject(valueYamlObj);
    }
}
