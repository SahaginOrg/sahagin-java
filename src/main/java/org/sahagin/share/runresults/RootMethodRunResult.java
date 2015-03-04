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
    public static final int WIDTH_NOT_ASSIGNED = -1;
    public static final int HEIGHT_NOT_ASSIGNED = -1;
    private static final String MSG_SRC_TREE_FORMAT_MISMATCH
    = "expected formatVersion is \"%s\", but actual is \"%s\"";

    private String rootMethodKey;
    private TestMethod rootMethod;
    private List<RunFailure> runFailures = new ArrayList<RunFailure>(16);
    private List<LineScreenCapture> lineScreenCaptures = new ArrayList<LineScreenCapture>(32);
    private int screenWidth = WIDTH_NOT_ASSIGNED;
    private int screenHeight = HEIGHT_NOT_ASSIGNED;

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

    public boolean isScreenWidthAssigned() {
        return screenWidth != WIDTH_NOT_ASSIGNED;
    }

    // WIDTH_NOT_ASSIGNED may be returned
    public int getScreenWidth() {
        return screenWidth;
    }

    // can set WIDTH_NOT_ASSIGNED
    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public boolean isScreenHeightAssigned() {
        return screenHeight != HEIGHT_NOT_ASSIGNED;
    }

    // HEIGHT_NOT_ASSIGNED may be returned
    public int getScreenHeight() {
        return screenHeight;
    }

    // can set HEIGHT_NOT_ASSIGNED
    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
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
        if (screenWidth != WIDTH_NOT_ASSIGNED) {
            result.put("screenWidth", screenWidth);
        }
        if (screenHeight != HEIGHT_NOT_ASSIGNED) {
            result.put("screenHeight", screenHeight);
        }
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
        Integer screenWidthObj = YamlUtils.getIntValue(yamlObject, "screenWidth", true);
        if (screenWidthObj != null) {
            screenWidth = screenWidthObj;
        } else {
            screenWidth = WIDTH_NOT_ASSIGNED;
        }
        Integer screenHeightObj = YamlUtils.getIntValue(yamlObject, "screenHeight", true);
        if (screenHeightObj != null) {
            screenHeight = screenHeightObj;
        } else {
            screenHeight = HEIGHT_NOT_ASSIGNED;
        }
    }

}
