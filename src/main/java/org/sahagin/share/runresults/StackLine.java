package org.sahagin.share.runresults;

import java.util.HashMap;
import java.util.Map;

import org.sahagin.share.srctree.TestFunction;
import org.sahagin.share.yaml.YamlUtils;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlConvertible;

// represents code line in the function codeBody for the codeBodyIndex
public class StackLine implements YamlConvertible {
    private String functionKey;
    private TestFunction function;
    private int line;
    private int codeBodyIndex;

    public String getFunctionKey() {
        return functionKey;
    }

    public void setFunctionKey(String functionKey) {
        this.functionKey = functionKey;
    }

    public TestFunction getFunction() {
        return function;
    }

    public void setFunction(TestFunction function) {
        this.function = function;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getCodeBodyIndex() {
        return codeBodyIndex;
    }

    public void setCodeBodyIndex(int codeBodyIndex) {
        this.codeBodyIndex = codeBodyIndex;
    }

    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = new HashMap<String, Object>(8);
        result.put("functionKey", functionKey);
        result.put("line", line);
        result.put("codeBodyIndex", codeBodyIndex);
        return result;
    }

    public void fromYamlObject(Map<String, Object> yamlObject) throws YamlConvertException {
        functionKey = YamlUtils.getStrValue(yamlObject, "functionKey");
        function = null;
        line = YamlUtils.getIntValue(yamlObject, "line");
        codeBodyIndex = YamlUtils.getIntValue(yamlObject, "codeBodyIndex");
    }

}
