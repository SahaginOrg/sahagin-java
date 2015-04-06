package org.sahagin.share.srctree.code;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlUtils;

public class TestStepLabel extends Code {
    public static final String TYPE = "stepLabel";

    private String label;
    private String text;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    protected String getType() {
        return TYPE;
    }

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = super.toYamlObject();
        if (label != null) {
            result.put("label", label);
        }
        if (text != null) {
            result.put("text", text);
        }

        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        super.fromYamlObject(yamlObject);
        label = YamlUtils.getStrValue(yamlObject, "label", true);
        text = YamlUtils.getStrValue(yamlObject, "text", true);
    }

}
