package org.sahagin.main;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;

import org.apache.commons.io.FileUtils;
import org.sahagin.runlib.external.adapter.AdapterContainer;
import org.sahagin.runlib.external.adapter.junit4.JUnit4Adapter;
import org.sahagin.runlib.external.adapter.webdriver.WebDriverAdapter;
import org.sahagin.runlib.runresultsgen.TestClassFileTransformer;
import org.sahagin.runlib.srctreegen.SrcTreeGenerator;
import org.sahagin.share.AcceptableLocales;
import org.sahagin.share.CommonPath;
import org.sahagin.share.Config;
import org.sahagin.share.IllegalTestScriptException;
import org.sahagin.share.Logging;
import org.sahagin.share.SrcTreeChecker;
import org.sahagin.share.srctree.SrcTree;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlUtils;

//provides premain method to generate SrcTree and RunResults and
//generate HTML report from them
public class SahaginPreMain {

    // agentArgs is configuration YAML file path
    public static void premain(String agentArgs, Instrumentation inst)
            throws YamlConvertException, IllegalTestScriptException, IOException {
        String configFilePath;
        if (agentArgs == null) {
            configFilePath = "sahagin.yml";
        } else {
            configFilePath = agentArgs;
        }
        Config config = Config.generateFromYamlConfig(new File(configFilePath));
        // TODO change adapter according to the configuration value
        new JUnit4Adapter().initialSetAdapter();
        new WebDriverAdapter().initialSetAdapter();

        Logging.setLoggerEnabled(config.isOutputLog());

        // delete previous data
        if (config.getRootBaseReportInputDataDir().exists()) {
            FileUtils.deleteDirectory(config.getRootBaseReportInputDataDir());
        }

        SrcTree srcTree = generateAndDumpSrcTree(config);
        TestClassFileTransformer transformer = new TestClassFileTransformer(configFilePath, srcTree);
        inst.addTransformer(transformer);
    }

    private static SrcTree generateAndDumpSrcTree(Config config) throws IllegalTestScriptException {
    	AcceptableLocales locales = AcceptableLocales.getInstance(config.getUserLocale());
        // generate and dump srcTree
        SrcTreeGenerator generator = new SrcTreeGenerator(
                AdapterContainer.globalInstance().getAdditionalTestDocs(), locales);
        File srcTreeFile = CommonPath.srcTreeFile(config.getRootBaseReportInputDataDir());
        SrcTree srcTree = generator.generateWithRuntimeClassPath(config.getRootBaseTestDir(), "UTF-8");
        SrcTreeChecker.check(srcTree);
        YamlUtils.dump(srcTree.toYamlObject(), srcTreeFile);
        return srcTree;
    }

}
