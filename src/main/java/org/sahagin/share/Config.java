package org.sahagin.share;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.sahagin.runlib.external.Locale;
import org.sahagin.share.yaml.YamlUtils;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlConvertible;

public class Config implements YamlConvertible {
    private static final String INVALID_CONFIG_YAML = "failed to load config file \"%s\": %s";
    private static final File REPORT_INTERMEDIATE_DATA_DIR_DEFAULT = new File("sahagin-intermediate-data");
    private static final File REPORT_OUTPUDT_DATA_DIR_DEFAULT = new File("sahagin-report");

    private File rootDir;
    private File reportIntermediateDataDir = REPORT_INTERMEDIATE_DATA_DIR_DEFAULT;
    private File reportOutputDir = REPORT_OUTPUDT_DATA_DIR_DEFAULT;
    private boolean outputLog = false; // TODO provisional. this is only for debugging
    // if true, don't generate report, generate only report input
    private boolean runTestOnly = false;
    private Locale userLocale = Locale.getSystemLocale();
    private boolean usesSystemLocale = true;

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

    protected final File getRootDir() {
        return rootDir;
    }

    public final File getRootBaseReportIntermediateDataDir() {
        if (reportIntermediateDataDir.isAbsolute()) {
            return reportIntermediateDataDir;
        } else {
            return new File(rootDir, reportIntermediateDataDir.getPath());
        }
    }

    public final void setReportIntermediateDataDir(File reportIntermediateDataDir) {
        this.reportIntermediateDataDir = reportIntermediateDataDir;
    }

    public final File getRootBaseReportOutputDir() {
        if (reportOutputDir.isAbsolute()) {
            return reportOutputDir;
        } else {
            return new File(rootDir, reportOutputDir.getPath());
        }
    }

    public final void setReportOutputDir(File reportOutputDir) {
        this.reportOutputDir = reportOutputDir;
    }

    public final boolean isOutputLog() {
        return outputLog;
    }

    public final void setOutputLog(boolean outputLog) {
        this.outputLog = outputLog;
    }

    public final boolean isRunTestOnly() {
        return runTestOnly;
    }

    public final void setRunTestOnly(boolean runTestOnly) {
        this.runTestOnly = runTestOnly;
    }

    public final Locale getUserLocale() {
        return userLocale;
    }

    public final boolean usesSystemLocale() {
        return usesSystemLocale;
    }

    public final void setUserLocale(Locale userLocale) {
        this.userLocale = userLocale;
        usesSystemLocale = false;
    }

    public final void setUserLocaleFromSystemLocale() {
        this.userLocale = Locale.getSystemLocale();
        usesSystemLocale = true;
    }

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> commonConf = new HashMap<String, Object>(4);
        commonConf.put("reportIntermediateDataDir", reportIntermediateDataDir.getPath());
        commonConf.put("reportOutputDir", reportOutputDir.getPath());
        commonConf.put("outputLog", outputLog);
        commonConf.put("runTestOnly", runTestOnly);
        if (usesSystemLocale) {
            commonConf.put("userLocale", "system");
        } else {
            commonConf.put("userLocale", userLocale.getValue());
        }
        Map<String, Object> result = new HashMap<String, Object>(4);
        result.put("common", commonConf);
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        // common and it's child settings is not mandatory
        Map<String, Object> commonYamlObj = YamlUtils.getYamlObjectValue(yamlObject, "common", true);
        if (commonYamlObj == null) {
            return;
        }

        String reportInputDataDirValue = YamlUtils.getStrValue(commonYamlObj, "reportIntermediateDataDir", true);
        if (reportInputDataDirValue != null) {
            reportIntermediateDataDir = new File(reportInputDataDirValue);
        } else {
            reportIntermediateDataDir = REPORT_INTERMEDIATE_DATA_DIR_DEFAULT;
        }

        String reportOutputDirValue = YamlUtils.getStrValue(commonYamlObj, "reportOutputDir", true);
        if (reportOutputDirValue != null) {
            reportOutputDir = new File(reportOutputDirValue);
        } else {
            reportOutputDir = REPORT_OUTPUDT_DATA_DIR_DEFAULT;
        }

        Boolean outputLogValue = YamlUtils.getBooleanValue(commonYamlObj, "outputLog", true);
        if (outputLogValue != null) {
            outputLog = outputLogValue;
        } else {
            outputLog = false;
        }

        Boolean runTestOnlyValue = YamlUtils.getBooleanValue(commonYamlObj, "runTestOnly", true);
        if (runTestOnlyValue != null) {
            runTestOnly = runTestOnlyValue;
        } else {
            runTestOnly = false;
        }

        String userLocaleValueStr = YamlUtils.getStrValue(commonYamlObj, "userLocale", true);
        if (userLocaleValueStr == null || userLocaleValueStr.equals("system")) {
            usesSystemLocale = true;
            userLocale = Locale.getSystemLocale();
        } else {
            usesSystemLocale = false;
            userLocale = YamlUtils.getLocaleValue(commonYamlObj, "userLocale");
        }
    }

}
