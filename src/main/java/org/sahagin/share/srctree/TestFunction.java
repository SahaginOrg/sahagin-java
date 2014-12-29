package org.sahagin.share.srctree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sahagin.runlib.external.CaptureStyle;
import org.sahagin.share.srctree.code.CodeLine;
import org.sahagin.share.yaml.YamlUtils;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlConvertible;

public class TestFunction implements YamlConvertible {
    public static final String MSG_INVALID_TYPE = "invalid type: %s";
    public static final String TYPE = "function";

    private String key;
    // qualifiedName is not necessarily unique
    private String qualifiedName;
    private String testDoc;
    private CaptureStyle captureStyle = CaptureStyle.THIS_LINE;
    private List<String> argVariables = new ArrayList<String>(4);
    private List<CodeLine> codeBody = new ArrayList<CodeLine>(32);

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSimpleName() {
        if (qualifiedName == null) {
            return null;
        }
        int lastIndex = qualifiedName.lastIndexOf("."); // TODO name separator is always dot ??
        if (lastIndex == -1) {
            return qualifiedName;
        }
        return qualifiedName.substring(lastIndex + 1);
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public String getTestDoc() {
        return testDoc;
    }

    public void setTestDoc(String testDoc) {
        this.testDoc = testDoc;
    }

    public CaptureStyle getCaptureStyle() {
        return captureStyle;
    }

    public void setCaptureStyle(CaptureStyle captureStyle) {
        if (captureStyle == CaptureStyle.NONE || captureStyle == CaptureStyle.STEP_IN_ONLY) {
            throw new RuntimeException("not supported yet: " + captureStyle);
        }
        this.captureStyle = captureStyle;
    }

    public List<String> getArgVariables() {
        return argVariables;
    }

    public void addArgVariable(String argVariable) {
        argVariables.add(argVariable);
    }

    public List<CodeLine> getCodeBody() {
        return codeBody;
    }

    public void addCodeBody(CodeLine codeLine) {
        codeBody.add(codeLine);
    }

    protected String getType() {
        return TYPE;
    }

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = new HashMap<String, Object>(8);
        result.put("type", getType());
        result.put("key", key);
        result.put("name", qualifiedName);
        result.put("testDoc", testDoc);
        result.put("capture", captureStyle.getValue());
        result.put("argVariables", argVariables);
        result.put("codeBody", YamlUtils.toYamlObjectList(codeBody));
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        YamlUtils.strValueEqualsCheck(yamlObject, "type", getType());
        key = YamlUtils.getStrValue(yamlObject, "key");
        qualifiedName = YamlUtils.getStrValue(yamlObject, "name");
        testDoc = YamlUtils.getStrValue(yamlObject, "testDoc");
        // captureStyle is not mandatory
        captureStyle = YamlUtils.getCaptureStyleValue(yamlObject, "capture", true);
        if (captureStyle == null) {
            captureStyle = CaptureStyle.THIS_LINE;
        }
        argVariables = YamlUtils.getStrListValue(yamlObject, "argVariables");
        List<Map<String, Object>> codeBodyYamlObj = YamlUtils.getYamlObjectListValue(yamlObject, "codeBody");
        codeBody = new ArrayList<CodeLine>(codeBodyYamlObj.size());
        for (Map<String, Object> codeLineYamlObj : codeBodyYamlObj) {
            CodeLine codeLine = new CodeLine();
            codeLine.fromYamlObject(codeLineYamlObj);
            codeBody.add(codeLine);
        }
    }

    public static TestFunction newInstanceFromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        String type = YamlUtils.getStrValue(yamlObject, "type");
        TestFunction result;
        if (TestFunction.TYPE.equals(type)) {
            result = new TestFunction();
        } else if (TestMethod.TYPE.equals(type)) {
            result = new TestMethod();
        } else {
            throw new YamlConvertException(String.format(MSG_INVALID_TYPE, type));
        }
        result.fromYamlObject(yamlObject);
        return result;
    }

}
