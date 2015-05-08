package org.sahagin.share.srctree.code;

import java.util.Map;

import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlUtils;

public class LocalVarAssign extends Code {
    public static final String TYPE = "localVarAssign";
    private String name;
    private Code value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        result.put("name", name);
        result.put("value", YamlUtils.toYamlObject(value));
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        super.fromYamlObject(yamlObject);
        name = YamlUtils.getStrValue(yamlObject, "name");
        Map<String, Object> valueYamlObj = YamlUtils.getYamlObjectValue(yamlObject, "value");
        value = Code.newInstanceFromYamlObject(valueYamlObj);
    }

}
