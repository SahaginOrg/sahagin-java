package org.sahagin.share.runresults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sahagin.share.CommonUtils;
import org.sahagin.share.srctree.TestMethod;
import org.sahagin.share.yaml.YamlUtils;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlConvertible;

public class RootMethodRunResult implements YamlConvertible {
    private static final String MSG_SRC_TREE_FORMAT_MISMATCH
    = "expected formatVersion is \"%s\", but actual is \"%s\"";

    private String rootMethodKey;
    private TestMethod rootMethod;
    private List<RunFailure> runFailures = new ArrayList<RunFailure>(16);
    private List<LineScreenCapture> lineScreenCaptures = new ArrayList<LineScreenCapture>(32);
    private int executionTime;

    public String getRootMethodKey() {
        return rootMethodKey;
    }

    public void setRootMethodKey(String rootMethodKey) {
        this.rootMethodKey = rootMethodKey;
    }

    public TestMethod getRootMethod() {
        return rootMethod;
    }

    public void setRootMethod(TestMethod rootMethod) {
        this.rootMethod = rootMethod;
    }

    public List<RunFailure> getRunFailures() {
        return runFailures;
    }

    public void addRunFailure(RunFailure runFailure) {
        this.runFailures.add(runFailure);
    }

    public List<LineScreenCapture> getLineScreenCaptures() {
        return lineScreenCaptures;
    }

    public void addLineScreenCapture(LineScreenCapture lineScreenCapture) {
        this.lineScreenCaptures.add(lineScreenCapture);
    }

    public void setExecutionTime(int executionTime) {
        this.executionTime = executionTime;
    }

    public int getExecutionTime() {
        return executionTime;
    }

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = new HashMap<String, Object>(4);
        result.put("formatVersion", CommonUtils.formatVersion());
        result.put("rootMethodKey", rootMethodKey);
        if (!runFailures.isEmpty()) {
            result.put("runFailures", YamlUtils.toYamlObjectList(runFailures));
        }
        if (!lineScreenCaptures.isEmpty()) {
            result.put("lineScreenCaptures", YamlUtils.toYamlObjectList(lineScreenCaptures));
        }
        result.put("executionTime", executionTime);
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        String formatVersion = YamlUtils.getStrValue(yamlObject, "formatVersion");
        // "*" means arbitrary version (this is only for testing sahagin itself)
        if (!formatVersion.equals("*")
                && !formatVersion.equals(CommonUtils.formatVersion())) {
            throw new YamlConvertException(String.format
                    (MSG_SRC_TREE_FORMAT_MISMATCH, CommonUtils.formatVersion(), formatVersion));
        }
        rootMethodKey = YamlUtils.getStrValue(yamlObject, "rootMethodKey");
        List<Map<String, Object>> runFailuresYamlObj
        = YamlUtils.getYamlObjectListValue(yamlObject, "runFailures", true);
        runFailures = new ArrayList<RunFailure>(runFailuresYamlObj.size());
        for (Map<String, Object> runFailureYamlObj : runFailuresYamlObj) {
            RunFailure runFailure = new RunFailure();
            runFailure.fromYamlObject(runFailureYamlObj);
            runFailures.add(runFailure);
        }
        List<Map<String, Object>> lineScreenCapturesYamlObj
        = YamlUtils.getYamlObjectListValue(yamlObject, "lineScreenCaptures", true);
        lineScreenCaptures = new ArrayList<LineScreenCapture>(lineScreenCapturesYamlObj.size());
        for (Map<String, Object> lineScreenCaptureYamlObj : lineScreenCapturesYamlObj) {
            LineScreenCapture lineScreenCapture = new LineScreenCapture();
            lineScreenCapture.fromYamlObject(lineScreenCaptureYamlObj);
            lineScreenCaptures.add(lineScreenCapture);
        }
        // TODO should accept empty executionTime
        executionTime = YamlUtils.getIntValue(yamlObject, "executionTime");
    }
}
