package org.sahagin.share.srctree.code;

import java.util.HashMap;
import java.util.Map;

import org.sahagin.share.srctree.ASTData;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlConvertible;
import org.sahagin.share.yaml.YamlUtils;

public class CodeLine extends ASTData implements YamlConvertible {
    // line start from 1
    private int startLine; // -1 means no information
    private int endLine; // -1 means no information
    private Code code;

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = new HashMap<String, Object>(4);
        result.put("startLine", startLine);
        result.put("endLine", endLine);
        result.put("code", YamlUtils.toYamlObject(code));
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject) throws YamlConvertException {
        startLine = YamlUtils.getIntValue(yamlObject, "startLine");
        endLine = YamlUtils.getIntValue(yamlObject, "endLine");
        code = Code.newInstanceFromYamlObject(YamlUtils.getYamlObjectValue(yamlObject, "code"));
    }
}
