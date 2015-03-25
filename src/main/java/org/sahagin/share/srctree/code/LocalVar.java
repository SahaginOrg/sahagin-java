package org.sahagin.share.srctree.code;

import java.util.Map;

import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlUtils;

public class LocalVar extends Code {
    public static final String TYPE = "localVar";
    private String name;

    @Override
    protected String getType() {
        return TYPE;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = super.toYamlObject();
        result.put("name", name);
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        super.fromYamlObject(yamlObject);
        name = YamlUtils.getStrValue(yamlObject, "name");
    }

}
