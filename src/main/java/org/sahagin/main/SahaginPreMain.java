package org.sahagin.main;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;

import org.apache.commons.io.FileUtils;
import org.sahagin.runlib.external.adapter.Adapter;
import org.sahagin.runlib.external.adapter.AdapterContainer;
import org.sahagin.runlib.external.adapter.appium.AppiumAdapter;
import org.sahagin.runlib.external.adapter.fluentlenium.FluentLeniumAdapter;
import org.sahagin.runlib.external.adapter.iosdriver.IOSDriverAdapter;
import org.sahagin.runlib.external.adapter.javalib.JavaLibAdapter;
import org.sahagin.runlib.external.adapter.junit3.JUnit3Adapter;
import org.sahagin.runlib.external.adapter.junit4.JUnit4Adapter;
import org.sahagin.runlib.external.adapter.selendroid.SelendroidAdapter;
import org.sahagin.runlib.external.adapter.testng.TestNGAdapter;
import org.sahagin.runlib.external.adapter.webdriver.WebDriverAdapter;
import org.sahagin.runlib.runresultsgen.RunResultsGenerateHookSetter;
import org.sahagin.runlib.srctreegen.SrcTreeGenerator;
import org.sahagin.share.AcceptableLocales;
import org.sahagin.share.CommonPath;
import org.sahagin.share.IllegalTestScriptException;
import org.sahagin.share.JavaConfig;
import org.sahagin.share.Logging;
import org.sahagin.share.SrcTreeChecker;
import org.sahagin.share.SysMessages;
import org.sahagin.share.srctree.SrcTree;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlUtils;

//provides premain method to generate SrcTree and RunResults and
//generate HTML report from them
public class SahaginPreMain {
    private static final String MSG_TEST_FRAMEWORK_NOT_FOUND
    = "testFramework not found: %s";

    // agentArgs is configuration YAML file path
    public static void premain(String agentArgs, Instrumentation inst)
            throws YamlConvertException, IllegalTestScriptException,
            IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String configFilePath;
        if (agentArgs == null) {
            configFilePath = "sahagin.yml";
        } else {
            configFilePath = agentArgs;
        }
        JavaConfig config = JavaConfig.generateFromYamlConfig(new File(configFilePath));
        Logging.setLoggerEnabled(config.isOutputLog());
        AcceptableLocales locales = AcceptableLocales.getInstance(config.getUserLocale());
        AdapterContainer.globalInitialize(locales, config.getTestFramework());
        SysMessages.globalInitialize(locales);

        // default adapters
        new JUnit3Adapter().initialSetAdapter();
        new JUnit4Adapter().initialSetAdapter();
        new TestNGAdapter().initialSetAdapter();
        new JavaLibAdapter().initialSetAdapter();
        new WebDriverAdapter().initialSetAdapter();
        new AppiumAdapter().initialSetAdapter();
        new SelendroidAdapter().initialSetAdapter();
        new IOSDriverAdapter().initialSetAdapter();
        new FluentLeniumAdapter().initialSetAdapter();

        for (String adapterClassName : config.getAdapterClassNames()) {
            // TODO handle exception thrown by forName or newInstance method
            // more appropriately
            Class<?> adapterClass = Class.forName(adapterClassName);
            assert adapterClass != null;
            Object adapterObj = adapterClass.newInstance();
            assert adapterObj != null;
            assert adapterObj instanceof Adapter;
            Adapter adapter = (Adapter) adapterObj;
            adapter.initialSetAdapter();
        }

        if (!AdapterContainer.globalInstance().isRootMethodAdapterSet()) {
            throw new RuntimeException(String.format(
                    MSG_TEST_FRAMEWORK_NOT_FOUND, config.getTestFramework()));
        }

        // delete previous data
        if (config.getRootBaseReportIntermediateDataDir().exists()) {
            FileUtils.deleteDirectory(config.getRootBaseReportIntermediateDataDir());
        }

        SrcTree srcTree = generateAndDumpSrcTree(config, locales);
        RunResultsGenerateHookSetter transformer = new RunResultsGenerateHookSetter(configFilePath, srcTree);
        inst.addTransformer(transformer);
    }

    private static SrcTree generateAndDumpSrcTree(JavaConfig config, AcceptableLocales locales)
            throws IllegalTestScriptException {
        // generate and dump srcTree
        SrcTreeGenerator generator = new SrcTreeGenerator(
                AdapterContainer.globalInstance().getAdditionalTestDocs(), locales);
        File srcTreeFile = CommonPath.srcTreeFile(config.getRootBaseReportIntermediateDataDir());
        SrcTree srcTree = generator.generateWithRuntimeClassPath(config.getRootBaseTestDir(), "UTF-8");
        SrcTreeChecker.check(srcTree);
        YamlUtils.dump(srcTree.toYamlObject(), srcTreeFile);
        return srcTree;
    }

}
