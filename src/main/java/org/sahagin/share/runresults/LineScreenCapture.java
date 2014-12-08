package org.sahagin.share.runresults;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sahagin.share.yaml.YamlUtils;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlConvertible;

public class LineScreenCapture implements YamlConvertible {
    // should use absolute path
    private File path;
    // head element means stack top
    private List<StackLine> stackLines = new ArrayList<StackLine>(16);

    public File getPath() {
        return path;
    }

    public void setPath(File path) {
        this.path = path;
    }

    public List<StackLine> getStackLines() {
        return stackLines;
    }

    public void addStackLine(StackLine stackLine) {
        stackLines.add(stackLine);
    }

    // check if stack line for this instance matches targetStackLines
    public boolean matchesStackLines(List<StackLine> targetStackLines) {
        if (targetStackLines.size() != getStackLines().size()) {
            return false;
        }
        for (int i = 0; i < targetStackLines.size(); i++) {
            StackLine targetLine = targetStackLines.get(i);
            StackLine line = getStackLines().get(i);
            if (!targetLine.getFunction().getKey().equals(line.getFunction().getKey())) {
                return false;
            }
            if (targetLine.getCodeBodyIndex() != line.getCodeBodyIndex()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = new HashMap<String, Object>(2);
        result.put("path", path.getPath());
        result.put("stackLines", YamlUtils.toYamlObjectList(stackLines));
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        path = new File(YamlUtils.getStrValue(yamlObject, "path"));
        List<Map<String, Object>> stackLinesYamlObj
        = YamlUtils.getYamlObjectListValue(yamlObject, "stackLines");
        stackLines = new ArrayList<StackLine>(stackLinesYamlObj.size());
        for (Map<String, Object> stackLineYamlObj : stackLinesYamlObj) {
            StackLine stackLine = new StackLine();
            stackLine.fromYamlObject(stackLineYamlObj);
            stackLines.add(stackLine);
        }
    }

}
