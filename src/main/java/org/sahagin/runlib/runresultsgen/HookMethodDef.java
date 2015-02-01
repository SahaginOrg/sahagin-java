package org.sahagin.runlib.runresultsgen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.io.IOUtils;
import org.sahagin.report.HtmlReport;
import org.sahagin.runlib.external.CaptureStyle;
import org.sahagin.runlib.external.adapter.AdapterContainer;
import org.sahagin.share.CommonPath;
import org.sahagin.share.Config;
import org.sahagin.share.IllegalDataStructureException;
import org.sahagin.share.IllegalTestScriptException;
import org.sahagin.share.Logging;
import org.sahagin.share.runresults.LineScreenCapture;
import org.sahagin.share.runresults.RootMethodRunResult;
import org.sahagin.share.runresults.RunFailure;
import org.sahagin.share.runresults.StackLine;
import org.sahagin.share.srctree.SrcTree;
import org.sahagin.share.srctree.TestMethod;
import org.sahagin.share.srctree.code.Code;
import org.sahagin.share.srctree.code.CodeLine;
import org.sahagin.share.srctree.code.SubMethodInvoke;
import org.sahagin.share.srctree.code.UnknownCode;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlUtils;

// TODO support multiple thread concurrent test execution

public class HookMethodDef {
    private static Logger logger = Logging.getLogger(HookMethodDef.class.getName());
    private static boolean initialized = false;
    private static File runResultsRootDir;
    private static File captureRootDir;
    private static int currentCaptureNo = 1;
    private static RootMethodRunResult currentRunResult = null;
    private static SrcTree srcTree;

    // if called multiple times, just ignored
    public static void initialize(String configFilePath) {
        if (initialized) {
            return;
        }
        logger.info("initialize");

        final Config config;
        try {
            config = Config.generateFromYamlConfig(new File(configFilePath));
        } catch (YamlConvertException e) {
            throw new RuntimeException(e);
        }

        HookMethodDef.runResultsRootDir
        = CommonPath.runResultRootDir(config.getRootBaseReportIntermediateDataDir());
        HookMethodDef.captureRootDir
        = CommonPath.inputCaptureRootDir(config.getRootBaseReportIntermediateDataDir());
        final File srcTreeFile = CommonPath.srcTreeFile(config.getRootBaseReportIntermediateDataDir());

        // load srcTree from already dumped srcTree YAML
        srcTree = new SrcTree();
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

        initialized = true;
    }

    private static void initializedCheck() {
        if (!initialized) {
            throw new IllegalStateException("initialize first");
        }
    }

    // initialize runResult information
    public static void beforeRootMethodHook() {
        logger.info("beforeRootMethodHook: start");
        initializedCheck();
        // initialize current captureNo and runResult
        currentCaptureNo = 1;
        currentRunResult = new RootMethodRunResult();
        TestMethod rootMethod = StackLineUtils.getRootMethod(
                srcTree.getRootMethodTable(), Thread.currentThread().getStackTrace());
        if (rootMethod == null) {
            throw new RuntimeException("implementation error");
        }
        currentRunResult.setRootMethodKey(rootMethod.getKey());
        currentRunResult.setRootMethod(rootMethod);
        logger.info("beforeRootMethodHook: end");
    }

    // set up runFailure information
    public static void rootMethodErrorHook(Throwable e) {
        initializedCheck();
        RunFailure runFailure = new RunFailure();
        runFailure.setMessage(e.getClass().getCanonicalName() + ": " + e.getLocalizedMessage());
        runFailure.setStackTrace(ExceptionUtils.getStackTrace(e));
        List<StackLine> stackLines = StackLineUtils.getStackLines(srcTree, e.getStackTrace());
        for (StackLine stackLine : stackLines) {
            runFailure.addStackLine(stackLine);
        }
        currentRunResult.addRunFailure(runFailure);

        assert currentRunResult.getRootMethod() instanceof TestMethod;
        TestMethod rootMethod = currentRunResult.getRootMethod();
        captureScreenForStackLine(rootMethod, stackLines);
    }

    // write runResult to YAML file
    public static void afterRootMethodHook() {
        logger.info("afterRootMethodHook: start");
        initializedCheck();

        assert currentRunResult != null;
        TestMethod rootMethod = currentRunResult.getRootMethod();
        assert rootMethod.getTestClass() != null;
        File runResultFile = new File(String.format("%s/%s/%s",
                runResultsRootDir, rootMethod.getTestClass().getQualifiedName(),
                rootMethod.getSimpleName()));
        runResultFile.getParentFile().mkdirs();
        YamlUtils.dump(currentRunResult.toYamlObject(), runResultFile);

        // clear current captureNo and runResult
        currentCaptureNo = -1;
        currentRunResult = null;
        logger.info("afterRootMethodHook: end");
    }

    private static TestMethod topmostInvokedMethod(Code code) {
        if (!(code instanceof SubMethodInvoke)) {
            return null;
        }
        return ((SubMethodInvoke) code).getSubMethod();
    }

    public static void afterTestSubMethodHook(String classQualifiedName, String methodSimpleName) {
        initializedCheck();
        if (currentRunResult == null) {
            return; // maybe called outside of the root method
        }

        logger.info("afterTestSubMethodHook: start: " + classQualifiedName + "." + methodSimpleName);
        // The CodeLine for hooked line must not exist in SrcTree, and must not exist in stackLines
        List<StackLine> stackLines = StackLineUtils.getStackLines(
                srcTree, Thread.currentThread().getStackTrace());
        if (stackLines.size() == 0) {
            return; // maybe called outside of the root method
        }
        CodeLine thisCodeLine = stackLines.get(0).getMethod().getCodeBody().get(
                stackLines.get(0).getCodeBodyIndex());
        TestMethod thisCodeLineTopmostMethod = topmostInvokedMethod(thisCodeLine.getCode());
        // TODO thisCodeLineTopmostMethod can be null when invoked method is overridden method
        // and super method is not annotated. This is bug..
        if (thisCodeLineTopmostMethod == null
                || !thisCodeLineTopmostMethod.getQualifiedName().equals(
                        classQualifiedName + "." + methodSimpleName)) {
            logger.info("afterTestSubMethodHook: skip non topmost method: "
                        + thisCodeLine.getCode().getOriginal());
            return;
        }

        if (!canStepInCaptureTo(stackLines)) {
            logger.info("beforeCodeBodyHook: skip no StepInCapture method");
            return;
        }

        TestMethod rootMethod = currentRunResult.getRootMethod();
        File captureFile = captureScreen(rootMethod, stackLines);
        if (captureFile != null) {
            logger.info("afterTestSubMethodHook: end with capture " + captureFile.getName());
        }
    }

    public static void beforeRootCodeBodyHook(String classQualifiedName, String methodSimpleName,
            int line, int actualInsertedLine) {
        beforeCodeBodyHook(classQualifiedName, methodSimpleName, line, actualInsertedLine);
    }

    public static void beforeSubCodeBodyHook(String classQualifiedName, String methodSimpleName,
            int line, int actualInsertedLine) {
        beforeCodeBodyHook(classQualifiedName, methodSimpleName, line, actualInsertedLine);
    }

    private static void beforeCodeBodyHook(String classQualifiedName, String methodSimpleName,
            int line, int actualInsertedLine) {
        initializedCheck();
        if (currentRunResult == null) {
            return; // maybe called outside of the root method
        }

        logger.info("beforeCodeBodyHook: start: " + classQualifiedName + "." + methodSimpleName + ": " + line);
        List<StackLine> stackLines = StackLineUtils.getStackLinesReplacingActualLine(
                srcTree, Thread.currentThread().getStackTrace(),
                classQualifiedName, methodSimpleName, actualInsertedLine, line);
        if (stackLines.size() == 0) {
            throw new RuntimeException("implementation error");
        }

        CodeLine thisCodeLine = stackLines.get(0).getMethod().getCodeBody().get(
                stackLines.get(0).getCodeBodyIndex());
        if (thisCodeLine.getCode() instanceof UnknownCode) {
            logger.info("beforeCodeBodyHook: skip UnknownCode method: " + thisCodeLine.getCode().getOriginal());
            return;
        }
        if (!canStepInCaptureTo(stackLines)) {
            logger.info("beforeCodeBodyHook: skip no StepInCapture method");
            return;
        }

        // screen capture
        TestMethod rootMethod = currentRunResult.getRootMethod();
        File captureFile = captureScreen(rootMethod, stackLines);
        if (captureFile != null) {
            logger.info("beforeCodeBodyHook: end with capture " + captureFile.getName());
        }
    }

    // returns null if not executed
    private static File captureScreen(TestMethod rootMethod) {
        byte[] screenData = AdapterContainer.globalInstance().captureScreen();
        if (screenData == null) {
            return null;
        }

        File captureFile = new File(String.format("%s/%s/%s/%03d.png",
                captureRootDir, rootMethod.getTestClass().getQualifiedName(),
                rootMethod.getSimpleName(), currentCaptureNo));
        currentCaptureNo++;

        captureFile.getParentFile().mkdirs();
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(captureFile);
            stream.write(screenData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
        return captureFile;
    }

    // - returns null if fails to capture
    // - try to capture even for UnknownCode and no stepInCapture line
    //   as long as code line exists in srcTree
    private static File captureScreenForStackLine(
            TestMethod rootMethod, List<StackLine> stackLines) {
        File captureFile = captureScreen(rootMethod);
        if (captureFile == null) {
            return null;
        }
        LineScreenCapture capture = new LineScreenCapture();
        capture.setPath(new File(captureFile.getAbsolutePath()));
        capture.addAllStackLines(stackLines);
        currentRunResult.addLineScreenCapture(capture);
        return captureFile;
    }

    // returns screen capture file
    // returns null if fail to capture
    private static File captureScreen(TestMethod rootMethod, List<StackLine> stackLines) {
        File captureFile = captureScreen(rootMethod);
        if (captureFile == null) {
            return null;
        }

        LineScreenCapture capture = new LineScreenCapture();
        capture.setPath(new File(captureFile.getAbsolutePath()));
        for (StackLine stackLine : stackLines) {
            capture.addStackLine(stackLine);
        }
        currentRunResult.addLineScreenCapture(capture);
        return captureFile;
    }

    // if method is called from not stepInCapture line, then returns false.
    private static boolean canStepInCaptureTo(List<StackLine> stackLines) {
        // stack bottom line ( = root line) is always regarded as stepIn true line
        for (int i = 0; i < stackLines.size() - 1; i++) {
            StackLine stackLine = stackLines.get(i);
            CaptureStyle style = stackLine.getMethod().getCaptureStyle();
            if (style != CaptureStyle.STEP_IN && style != CaptureStyle.STEP_IN_ONLY) {
                return false;
            }
        }
        return true;
    }

}