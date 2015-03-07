package org.sahagin.share;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private File testDir;
    private List<String> adapterClassNames = new ArrayList<String>(8);
    private boolean usesJUnit3 = false;
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

    public List<String> getAdapterClassNames() {
        return adapterClassNames;
    }

    public void addAdapterClassName(String adapterClassName) {
        adapterClassNames.add(adapterClassName);
    }

    public boolean usesJUnit3() {
        return usesJUnit3;
    }

    public void setUsesJUnit3(boolean usesJUnit3) {
        this.usesJUnit3 = usesJUnit3;
    }

    public File getRootBaseReportIntermediateDataDir() {
        if (reportIntermediateDataDir.isAbsolute()) {
            return reportIntermediateDataDir;
        } else {
            return new File(rootDir, reportIntermediateDataDir.getPath());
        }
    }

    public void setReportIntermediateDataDir(File reportIntermediateDataDir) {
        this.reportIntermediateDataDir = reportIntermediateDataDir;
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

    public boolean isRunTestOnly() {
        return runTestOnly;
    }

    public void setRunTestOnly(boolean runTestOnly) {
        this.runTestOnly = runTestOnly;
    }

    public Locale getUserLocale() {
        return userLocale;
    }

    public boolean usesSystemLocale() {
        return usesSystemLocale;
    }

    public void setUserLocale(Locale userLocale) {
        this.userLocale = userLocale;
        usesSystemLocale = false;
    }

    public void setUserLocaleFromSystemLocale() {
        this.userLocale = Locale.getSystemLocale();
        usesSystemLocale = true;
    }

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> javaConf = new HashMap<String, Object>(4);
        javaConf.put("testDir", testDir.getPath());
        javaConf.put("adapters", adapterClassNames);
        javaConf.put("jUnit3", usesJUnit3);
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
        Map<String, Object> result = new HashMap<String, Object>(2);
        result.put("java", javaConf);
        result.put("common", commonConf);
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        Map<String, Object> javaYamlObj = YamlUtils.getYamlObjectValue(yamlObject, "java");
        // testDir for java is mandatory
        // (since cannot get source code path on run time)
        // TODO support array testDir value (so, testDir can be string or string array)
        testDir = new File(YamlUtils.getStrValue(javaYamlObj, "testDir"));
        adapterClassNames = YamlUtils.getStrListValue(javaYamlObj, "adapters", true);

        Boolean usesJUnit3Value = YamlUtils.getBooleanValue(javaYamlObj, "jUnit3", true);
        if (usesJUnit3Value == null) {
            usesJUnit3 = false;
        } else {
            usesJUnit3 = usesJUnit3Value;
        }

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
