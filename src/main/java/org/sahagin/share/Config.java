package org.sahagin.share;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.sahagin.share.yaml.YamlUtils;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlConvertible;

public class Config implements YamlConvertible {
    private static final String INVALID_CONFIG_YAML = "failed to load config file \"%s\": %s";

    private File rootDir;
    private File testDir;
    private File reportInputDataDir;
    private File reportOutputDir;
    private boolean outputLog = false; // TODO provisional. this is only for debugging

    public static Config generateFromYamlConfig(File yamlConfigFile) throws YamlConvertException {
        Map<String, Object> configYamlObj = YamlUtils.load(yamlConfigFile);
        // use the parent directory of yamlConfigFile as the root directory
        Config config = new Config(yamlConfigFile.getParentFile());
        try {
            config.fromYamlObject(configYamlObj);
        } catch (YamlConvertException e) {
            throw new YamlConvertException(String.format(
                    INVALID_CONFIG_YAML, yamlConfigFile.getAbsolutePath(), e.getLocalizedMessage()), e);
        }
        return config;
    }

    public Config(File rootDir) {
        this.rootDir = rootDir;
    }

    public File getRootBaseTestDir() {
        if (testDir.isAbsolute()) {
            return testDir;
        } else {
            return new File(rootDir, testDir.getPath());
        }
    }

    public void setTestDir(File testDir) {
        this.testDir = testDir;
    }

    public File getRootBaseReportInputDataDir() {
        if (reportInputDataDir.isAbsolute()) {
            return reportInputDataDir;
        } else {
            return new File(rootDir, reportInputDataDir.getPath());
        }
    }

    public void setReportInputDataDir(File reportInputDataDir) {
        this.reportInputDataDir = reportInputDataDir;
    }

    public File getRootBaseReportOutputDir() {
        if (reportOutputDir.isAbsolute()) {
            return reportOutputDir;
        } else {
            return new File(rootDir, reportOutputDir.getPath());
        }
    }

    public void setReportOutputDir(File reportOutputDir) {
        this.reportOutputDir = reportOutputDir;
    }

    public boolean isOutputLog() {
        return outputLog;
    }

    public void setOutputLog(boolean outputLog) {
        this.outputLog = outputLog;
    }

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = new HashMap<String, Object>(16);
        result.put("testDir", testDir.getPath());
        result.put("reportInputDataDir", reportInputDataDir.getPath());
        result.put("reportOutputDir", reportOutputDir.getPath());
        result.put("outputLog", outputLog);
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        testDir = new File(YamlUtils.getStrValue(yamlObject, "testDir"));
        reportInputDataDir = new File(YamlUtils.getStrValue(yamlObject, "reportInputDataDir"));
        reportOutputDir = new File(YamlUtils.getStrValue(yamlObject, "reportOutputDir"));
        // outputLog is not mandatory
        Boolean outputLogObj = YamlUtils.getBooleanValue(yamlObject, "outputLog", true);
        if (outputLogObj == null) {
            outputLog = false;
        } else {
            outputLog = outputLogObj;
        }
    }

}
