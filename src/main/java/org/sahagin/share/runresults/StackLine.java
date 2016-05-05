package org.sahagin.share.runresults;

import java.util.HashMap;
import java.util.Map;

import org.sahagin.share.srctree.TestMethod;
import org.sahagin.share.yaml.YamlUtils;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlConvertible;

// represents code line in the method codeBody for the codeBodyIndex
public class StackLine implements YamlConvertible {
    private String methodKey;
    private TestMethod method;
    private int line;
    private int codeBodyIndex;

    // normal constructor
    public StackLine() {
    }

    // Copy constructor.
    // Generate new copy of source instance. This copy is shallow copy.
    public StackLine(StackLine src) {
        this.methodKey = src.methodKey;
        this.method = src.method;
        this.line = src.line;
        this.codeBodyIndex = src.codeBodyIndex;
    }

    public String getMethodKey() {
        return methodKey;
    }

    public void setMethodKey(String methodKey) {
        this.methodKey = methodKey;
    }

    public TestMethod getMethod() {
        return method;
    }

    public void setMethod(TestMethod method) {
        this.method = method;
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

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = new HashMap<String, Object>(8);
        result.put("methodKey", methodKey);
        result.put("codeBodyIndex", codeBodyIndex);
        result.put("line", line);
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject) throws YamlConvertException {
        methodKey = YamlUtils.getStrValue(yamlObject, "methodKey");
        method = null;
        codeBodyIndex = YamlUtils.getIntValue(yamlObject, "codeBodyIndex");
        line = YamlUtils.getIntValue(yamlObject, "line");
    }

}
