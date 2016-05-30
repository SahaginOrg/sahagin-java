package org.sahagin.share;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sahagin.runlib.external.Locale;
import org.sahagin.share.yaml.YamlUtils;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlConvertible;

public class Config implements YamlConvertible {
    private static final String INVALID_CONFIG_YAML = "failed to load config file \"%s\": %s";
    private static final File INTERMEDIATE_DATA_DIR_DEFAULT = new File("sahagin-intermediate-data");
    private static final File REPORT_OUTPUDT_DATA_DIR_DEFAULT = new File("sahagin-report");

    private File rootDir;
    private File runOutputIntermediateDataDir = INTERMEDIATE_DATA_DIR_DEFAULT;
    private List<File> reportInputIntermediateDataDirs
    = new ArrayList<>(Arrays.asList(INTERMEDIATE_DATA_DIR_DEFAULT));
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

    public final File getRootBaseRunOutputIntermediateDataDir() {
        if (runOutputIntermediateDataDir.isAbsolute()) {
            return runOutputIntermediateDataDir;
        } else {
            return new File(rootDir, runOutputIntermediateDataDir.getPath());
        }
    }

    public final void setRootBaseRunOutputIntermediateDataDir(File runOutputIntermediateDataDir) {
        this.runOutputIntermediateDataDir = runOutputIntermediateDataDir;
    }

    public final List<File> getRootBaseReportInputIntermediateDataDirs() {
        List<File> result = new ArrayList<>(reportInputIntermediateDataDirs.size());
        for (File reportInputIntermediateDataDir : reportInputIntermediateDataDirs) {
            if (reportInputIntermediateDataDir.isAbsolute()) {
                result.add(reportInputIntermediateDataDir);
            } else {
                result.add(new File(rootDir, reportInputIntermediateDataDir.getPath()));
            }
        }
        return result;
    }

    public final void addRootBaseReportInputIntermediateDataDir(File reportInputIntermediateDataDir) {
        reportInputIntermediateDataDirs.add(reportInputIntermediateDataDir);
    }

    public final void clearRootBaseReportInputIntermediateDataDirs() {
        reportInputIntermediateDataDirs.clear();
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
        Map<String, Object> commonConf = new HashMap<>(4);
        commonConf.put("runOutputIntermediateDataDir", runOutputIntermediateDataDir.getPath());
        if (!reportInputIntermediateDataDirs.isEmpty()) {
            List<String> paths = new ArrayList<>(reportInputIntermediateDataDirs.size());
            for (File reportInputIntermediateDataDir : reportInputIntermediateDataDirs) {
                paths.add(reportInputIntermediateDataDir.getPath());
            }
            commonConf.put("reportInputIntermediateDataDirs", paths);
        }
        commonConf.put("reportOutputDir", reportOutputDir.getPath());
        commonConf.put("outputLog", outputLog);
        commonConf.put("runTestOnly", runTestOnly);
        if (usesSystemLocale) {
            commonConf.put("userLocale", "system");
        } else {
            commonConf.put("userLocale", userLocale.getValue());
        }
        Map<String, Object> result = new HashMap<>(4);
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

        String runOutputIntermediateDataDirValue
        = YamlUtils.getStrValue(commonYamlObj, "runOutputIntermediateDataDir", true);
        if (runOutputIntermediateDataDirValue == null) {
            runOutputIntermediateDataDir  = null;
        } else {
            runOutputIntermediateDataDir = new File(runOutputIntermediateDataDirValue);
        }
        List<String> reportInputIntermediateDataDirsValue
        = YamlUtils.getStrListValue(commonYamlObj, "reportInputIntermediateDataDirs", true);
        reportInputIntermediateDataDirs.clear();
        for (String reportInputIntermediateDataDirValue : reportInputIntermediateDataDirsValue) {
            reportInputIntermediateDataDirs.add(new File(reportInputIntermediateDataDirValue));
        }

        // set default values for runOutputIntermediateDataDir and reportInputIntermediateDataDirs
        // if they are empty
        if (runOutputIntermediateDataDir == null && reportInputIntermediateDataDirs.size() == 0) {
            runOutputIntermediateDataDir = INTERMEDIATE_DATA_DIR_DEFAULT;
            reportInputIntermediateDataDirs.add(INTERMEDIATE_DATA_DIR_DEFAULT);
        } else if (runOutputIntermediateDataDir == null) {
            runOutputIntermediateDataDir = reportInputIntermediateDataDirs.get(0);
        } else if (reportInputIntermediateDataDirs.size() == 0) {
            reportInputIntermediateDataDirs.add(runOutputIntermediateDataDir);
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
