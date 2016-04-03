package org.sahagin.runlib.runresultsgen;

import java.io.File;
import java.util.logging.Logger;

import org.sahagin.report.HtmlReport;
import org.sahagin.share.CommonPath;
import org.sahagin.share.Config;
import org.sahagin.share.IllegalDataStructureException;
import org.sahagin.share.IllegalTestScriptException;
import org.sahagin.share.JavaConfig;
import org.sahagin.share.Logging;
import org.sahagin.share.srctree.SrcTree;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlUtils;

// TODO support multiple thread concurrent test execution

public class HookMethodDef {
    private static Logger logger = Logging.getLogger(HookMethodDef.class.getName());
    private static HookMethodManager manager = null;

    // if called multiple times, just ignored
    public static void initialize(String configFilePath) {
        if (manager != null) {
            return;
        }

        logger.info("initialize");

        final Config config;
        try {
            config = JavaConfig.generateFromYamlConfig(new File(configFilePath));
        } catch (YamlConvertException e) {
            throw new RuntimeException(e);
        }

        // load srcTree from already dumped srcTree YAML
        final File srcTreeFile = CommonPath.srcTreeFile(config.getRootBaseReportIntermediateDataDir());
        SrcTree srcTree = new SrcTree();
        try {
            srcTree.fromYamlObject(YamlUtils.load(srcTreeFile));
        } catch (YamlConvertException e) {
            throw new RuntimeException(e);
        }
        try {
            srcTree.resolveKeyReference();
        } catch (IllegalDataStructureException e) {
            throw new RuntimeException(e);
        }

        manager = new HookMethodManager(srcTree, config);

        if (!config.isRunTestOnly()) {
            // set up shutdown hook which generates HTML report
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    HtmlReport report = new HtmlReport();
                    try {
                        report.generate(config.getRootBaseReportIntermediateDataDir(),
                                config.getRootBaseReportOutputDir());
                    } catch (IllegalDataStructureException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalTestScriptException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    private static void initializedCheck() {
        if (manager == null) {
            throw new IllegalStateException("initialize first");
        }
    }

    public static void beforeMethodHook(String hookedClassQualifiedName,
            String hookedMethodSimpleName, String actualHookedMethodSimpleName) {
        initializedCheck();
        manager.beforeMethodHook(
                hookedClassQualifiedName, hookedMethodSimpleName, actualHookedMethodSimpleName);
    }

    public static void methodErrorHook(
            String hookedClassQualifiedName, String hookedMethodSimpleName, Throwable e) {
        initializedCheck();
        manager.methodErrorHook(hookedClassQualifiedName, hookedMethodSimpleName, e);
    }

    public static void afterMethodHook(
            String hookedClassQualifiedName, String hookedMethodSimpleName) {
        initializedCheck();
        manager.afterMethodHook(hookedClassQualifiedName, hookedMethodSimpleName);
    }

    public static void beforeCodeLineHook(String hookedClassQualifiedName,
            String hookedMethodSimpleName, String actualHookedMethodSimpleName,
            String hookedArgClassesStr, int hookedLine, int actualInsertedLine) {
        initializedCheck();
        manager.beforeCodeLineHook(hookedClassQualifiedName,
                hookedMethodSimpleName, actualHookedMethodSimpleName,
                hookedArgClassesStr, hookedLine, actualInsertedLine);
    }

    public static void afterCodeLineHook(String hookedClassQualifiedName,
            String hookedMethodSimpleName, String actualHookedMethodSimpleName,
            String hookedArgClassesStr, int hookedLine, int actualInsertedLine) {
        initializedCheck();
        manager.afterCodeLineHook(hookedClassQualifiedName,
                hookedMethodSimpleName, actualHookedMethodSimpleName,
                hookedArgClassesStr, hookedLine, actualInsertedLine);
    }
}