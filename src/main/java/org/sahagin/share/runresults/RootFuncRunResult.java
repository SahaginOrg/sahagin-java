package org.sahagin.share.runresults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sahagin.share.CommonUtils;
import org.sahagin.share.srctree.TestFunction;
import org.sahagin.share.yaml.YamlUtils;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlConvertible;

public class RootFuncRunResult implements YamlConvertible {
    private String rootFunctionKey;
    private TestFunction rootFunction;
    private List<RunFailure> runFailures = new ArrayList<RunFailure>(16);
    private List<LineScreenCapture> lineScreenCaptures = new ArrayList<LineScreenCapture>(32);

    public String getRootFunctionKey() {
        return rootFunctionKey;
    }

    public void setRootFunctionKey(String rootFunctionKey) {
        this.rootFunctionKey = rootFunctionKey;
    }

    public TestFunction getRootFunction() {
        return rootFunction;
    }

    public void setRootFunction(TestFunction rootFunction) {
        this.rootFunction = rootFunction;
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

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = new HashMap<String, Object>(4);
        result.put("rootFunctionKey", rootFunctionKey);
        result.put("runFailures", YamlUtils.toYamlObjectList(runFailures));
        result.put("lineScreenCaptures", YamlUtils.toYamlObjectList(lineScreenCaptures));
        result.put("formatVersion", CommonUtils.formatVersion());
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        rootFunctionKey = YamlUtils.getStrValue(yamlObject, "rootFunctionKey");
        List<Map<String, Object>> runFailuresYamlObj
        = YamlUtils.getYamlObjectListValue(yamlObject, "runFailures");
        runFailures = new ArrayList<RunFailure>(runFailuresYamlObj.size());
        for (Map<String, Object> runFailureYamlObj : runFailuresYamlObj) {
            RunFailure runFailure = new RunFailure();
            runFailure.fromYamlObject(runFailureYamlObj);
            runFailures.add(runFailure);
        }
        List<Map<String, Object>> lineScreenCapturesYamlObj
        = YamlUtils.getYamlObjectListValue(yamlObject, "lineScreenCaptures");
        lineScreenCaptures = new ArrayList<LineScreenCapture>(lineScreenCapturesYamlObj.size());
        for (Map<String, Object> lineScreenCaptureYamlObj : lineScreenCapturesYamlObj) {
            LineScreenCapture lineScreenCapture = new LineScreenCapture();
            lineScreenCapture.fromYamlObject(lineScreenCaptureYamlObj);
            lineScreenCaptures.add(lineScreenCapture);
        }
    }

}
