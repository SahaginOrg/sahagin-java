package org.sahagin.share.runresults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sahagin.share.yaml.YamlUtils;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlConvertible;

public class RunFailure implements YamlConvertible {
    private String message;
    // this is display only string, and the format is arbitrary
    private String stackTrace;
    // head element means stack top
    private List<StackLine> stackLines = new ArrayList<StackLine>(16);

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public List<StackLine> getStackLines() {
        return stackLines;
    }

    public void addStackLine(StackLine stackLine) {
        stackLines.add(stackLine);
    }

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = new HashMap<String, Object>(4);
        if (message != null) {
            result.put("message", message);
        }
        if (stackTrace != null) {
            result.put("stackTrace", stackTrace);
        }
        if (!stackLines.isEmpty()) {
            result.put("stackLines", YamlUtils.toYamlObjectList(stackLines));
        }
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        message = YamlUtils.getStrValue(yamlObject, "message", true);
        stackTrace = YamlUtils.getStrValue(yamlObject, "stackTrace", true);
        List<Map<String, Object>> stackLinesYamlObj
        = YamlUtils.getYamlObjectListValue(yamlObject, "stackLines", true);
        stackLines = new ArrayList<StackLine>(stackLinesYamlObj.size());
        for (Map<String, Object> stackLineYamlObj : stackLinesYamlObj) {
            StackLine stackLine = new StackLine();
            stackLine.fromYamlObject(stackLineYamlObj);
            stackLines.add(stackLine);
        }
    }

}
