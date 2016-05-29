package org.sahagin.share.srctree.code;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlUtils;

public class TestStep extends Code {
    public static final String TYPE = "step";

    private String label;
    private String text;
    private List<CodeLine> stepBody = new ArrayList<CodeLine>(32);

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

    public List<CodeLine> getStepBody() {
        return stepBody;
    }

    public void addStepBody(CodeLine step) {
        stepBody.add(step);
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
        if (!stepBody.isEmpty()) {
            result.put("body", YamlUtils.toYamlObjectList(stepBody));
        }

        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        super.fromYamlObject(yamlObject);
        label = YamlUtils.getStrValue(yamlObject, "label", true);
        text = YamlUtils.getStrValue(yamlObject, "text", true);
        List<Map<String, Object>> stepBodyYamlObj = YamlUtils.getYamlObjectListValue(yamlObject, "body", true);
        stepBody = new ArrayList<CodeLine>(stepBodyYamlObj.size());
        for (Map<String, Object> stepYamlObj : stepBodyYamlObj) {
            CodeLine step = new CodeLine();
            step.fromYamlObject(stepYamlObj);
            stepBody.add(step);
        }
    }
}
