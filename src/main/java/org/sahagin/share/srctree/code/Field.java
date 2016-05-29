package org.sahagin.share.srctree.code;

import java.util.Map;

import org.sahagin.share.srctree.TestField;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlUtils;

public class Field extends Code {
    public static final String TYPE = "field";
    private String fieldKey;
    private TestField field;
    private Code thisInstance;

    public String getFieldKey() {
        return fieldKey;
    }

    public void setFieldKey(String fieldKey) {
        this.fieldKey = fieldKey;
    }

    public TestField getField() {
        return field;
    }

    public void setField(TestField field) {
        this.field = field;
    }

    public Code getThisInstance() {
        return thisInstance;
    }

    public void setThisInstance(Code thisInstance) {
        this.thisInstance = thisInstance;
    }

    @Override
    protected String getType() {
        return TYPE;
    }

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = super.toYamlObject();
        result.put("fieldKey", fieldKey);
        if (thisInstance != null) {
            result.put("thisInstance", YamlUtils.toYamlObject(thisInstance));
        }
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        super.fromYamlObject(yamlObject);
        fieldKey = YamlUtils.getStrValue(yamlObject, "fieldKey");
        field = null;
        Map<String, Object> thisInstanceYamlObj = YamlUtils.getYamlObjectValue(yamlObject, "thisInstance", true);
        thisInstance = Code.newInstanceFromYamlObject(thisInstanceYamlObj);
    }
}
