package org.sahagin.share.srctree.code;

import java.util.Map;

import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlUtils;

public class FuncArgument extends Code {
    public static final String TYPE = "arg";

    private int argIndex;

    @Override
    protected String getType() {
        return TYPE;
    }

    public int getArgIndex() {
        return argIndex;
    }

    public void setArgIndex(int argIndex) {
        this.argIndex = argIndex;
    }

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = super.toYamlObject();
        result.put("argIndex", argIndex);
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        super.fromYamlObject(yamlObject);
        argIndex = YamlUtils.getIntValue(yamlObject, "argIndex");
    }

}
