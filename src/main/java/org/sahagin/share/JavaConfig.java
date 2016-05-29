package org.sahagin.share;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sahagin.share.yaml.YamlUtils;
import org.sahagin.share.yaml.YamlConvertException;

public class JavaConfig extends Config {
    private static final String INVALID_CONFIG_YAML = "failed to load config file \"%s\": %s";
    private static final String TEST_FRAMEWORK_DEFAULT = "jUnit4";

    private File testDir;
    private List<String> adapterClassNames = new ArrayList<>(8);
    private String testFramework = TEST_FRAMEWORK_DEFAULT;

    public static JavaConfig generateFromYamlConfig(File yamlConfigFile) throws YamlConvertException {
        Map<String, Object> configYamlObj = YamlUtils.load(yamlConfigFile);
        // use the parent directory of yamlConfigFile as the root directory
        JavaConfig config = new JavaConfig(yamlConfigFile.getParentFile());
        try {
            config.fromYamlObject(configYamlObj);
        } catch (YamlConvertException e) {
            throw new YamlConvertException(String.format(
                    INVALID_CONFIG_YAML, yamlConfigFile.getAbsolutePath(), e.getLocalizedMessage()), e);
        }
        return config;
    }

    public JavaConfig(File rootDir) {
        super(rootDir);
    }

    public File getRootBaseTestDir() {
        if (testDir.isAbsolute()) {
            return testDir;
        } else {
            return new File(getRootDir(), testDir.getPath());
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

    public String getTestFramework() {
        return testFramework;
    }

    public void setTestFramework(String testFramework) {
        this.testFramework = testFramework;
    }

    @Override
    public Map<String, Object> toYamlObject() {
        Map<String, Object> result = super.toYamlObject();
        Map<String, Object> javaConf = new HashMap<>(4);
        javaConf.put("testDir", testDir.getPath());
        javaConf.put("adapters", adapterClassNames);
        javaConf.put("testFramework", testFramework);
        result.put("java", javaConf);
        return result;
    }

    @Override
    public void fromYamlObject(Map<String, Object> yamlObject)
            throws YamlConvertException {
        super.fromYamlObject(yamlObject);
        Map<String, Object> javaYamlObj = YamlUtils.getYamlObjectValue(yamlObject, "java");
        // testDir for java is mandatory
        // (since cannot get source code path on run time)
        // TODO support array testDir value (so, testDir can be string or string array)
        testDir = new File(YamlUtils.getStrValue(javaYamlObj, "testDir"));
        adapterClassNames = YamlUtils.getStrListValue(javaYamlObj, "adapters", true);

        String testFrameworkValue = YamlUtils.getStrValue(javaYamlObj, "testFramework", true);
        if (testFrameworkValue == null) {
            testFrameworkValue = TEST_FRAMEWORK_DEFAULT;
        } else {
            testFramework = testFrameworkValue;
        }
    }
}
