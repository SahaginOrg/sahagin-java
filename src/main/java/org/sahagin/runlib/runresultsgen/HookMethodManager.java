package org.sahagin.runlib.runresultsgen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.io.IOUtils;
import org.sahagin.runlib.external.CaptureStyle;
import org.sahagin.runlib.external.adapter.AdapterContainer;
import org.sahagin.share.CommonPath;
import org.sahagin.share.Config;
import org.sahagin.share.IllegalDataStructureException;
import org.sahagin.share.Logging;
import org.sahagin.share.runresults.LineScreenCapture;
import org.sahagin.share.runresults.RootMethodRunResult;
import org.sahagin.share.runresults.RunFailure;
import org.sahagin.share.runresults.StackLine;
import org.sahagin.share.srctree.SrcTree;
import org.sahagin.share.srctree.TestMethod;
import org.sahagin.share.srctree.code.CodeLine;
import org.sahagin.share.srctree.code.Field;
import org.sahagin.share.srctree.code.SubMethodInvoke;
import org.sahagin.share.srctree.code.TestStep;
import org.sahagin.share.srctree.code.TestStepLabel;
import org.sahagin.share.srctree.code.VarAssign;
import org.sahagin.share.yaml.YamlUtils;

public class HookMethodManager {
    private static Logger logger = Logging.getLogger(HookMethodManager.class.getName());
    private SrcTree srcTree;
    private File runResultsRootDir;
    private File captureRootDir;
    private int currentCaptureNo = 1;
    private RootMethodRunResult currentRunResult = null;
    // method and key for last called beforeCodeLineHook is cached
    private String codeLineHookedMethodKeyCache = null;
    private TestMethod codeLineHookedMethodCache = null;

    public HookMethodManager(SrcTree srcTree, Config config) {
        if (srcTree == null) {
            throw new NullPointerException();
        }
        if (config == null) {
            throw new NullPointerException();
        }
        this.srcTree = srcTree;
        runResultsRootDir = CommonPath.runResultRootDir(config.getRootBaseReportIntermediateDataDir());
        captureRootDir = CommonPath.inputCaptureRootDir(config.getRootBaseReportIntermediateDataDir());
    }

    // initialize runResult information if the method for the arguments is root method
    public void beforeMethodHook(String hookedClassQualifiedName, String hookedMethodSimpleName) {
        if (currentRunResult != null) {
            return; // maybe called inside of the root method
        }

        List<TestMethod> rootMethods = srcTree.getRootMethodTable().getByName(
                hookedClassQualifiedName, hookedMethodSimpleName);
        if (rootMethods.size() == 0) {
            return; // hooked method is not root method
        }
        assert rootMethods.size() == 1;
        TestMethod rootMethod = rootMethods.get(0);

        logger.info("beforeMethodHook: " + hookedMethodSimpleName);

        // initialize current captureNo and runResult
        currentCaptureNo = 1;
        currentRunResult = new RootMethodRunResult();
        currentRunResult.setRootMethodKey(rootMethod.getKey());
        currentRunResult.setRootMethod(rootMethod);
    }

    // set up runFailure information
    // This method must be called before afterMethodHook is called
    public void methodErrorHook(String hookedClassQualifiedName,
            String hookedMethodSimpleName, Throwable e) {
        if (currentRunResult == null) {
            return; // maybe called outside of the root method
        }
        TestMethod rootMethod = currentRunResult.getRootMethod();
        if (!rootMethod.getTestClassKey().equals(hookedClassQualifiedName)
                || !rootMethod.getSimpleName().equals(hookedMethodSimpleName)) {
            return; // hooked method is not current root method
        }

        RunFailure runFailure = new RunFailure();
        runFailure.setMessage(e.getClass().getCanonicalName() + ": " + e.getLocalizedMessage());
        runFailure.setStackTrace(ExceptionUtils.getStackTrace(e));
        // TODO if groovy
        List<StackLine> stackLines = StackLineUtils.getStackLines(srcTree, e.getStackTrace());
        for (StackLine stackLine : stackLines) {
            runFailure.addStackLine(stackLine);
        }
        currentRunResult.addRunFailure(runFailure);

        // TODO if groovy
        captureScreenForStackLine(rootMethod, stackLines);
    }

    // write runResult to YAML file if the method for the arguments is root method
    public void afterMethodHook(String hookedClassQualifiedName, String hookedMethodSimpleName) {
        if (currentRunResult == null) {
            return; // maybe called outside of the root method
        }
        TestMethod rootMethod = currentRunResult.getRootMethod();
        if (!rootMethod.getTestClassKey().equals(hookedClassQualifiedName)
                || !rootMethod.getSimpleName().equals(hookedMethodSimpleName)) {
            return; // hooked method is not current root method
        }

        logger.info("afterMethodHook: " + hookedMethodSimpleName);

        // write runResult to YAML file
        File runResultFile = new File(String.format(
                "%s/%s/%s", runResultsRootDir, hookedClassQualifiedName, hookedMethodSimpleName));
        if (runResultFile.getParentFile() != null) {
            runResultFile.getParentFile().mkdirs();
        }
        YamlUtils.dump(currentRunResult.toYamlObject(), runResultFile);

        // clear current captureNo and runResult
        currentCaptureNo = -1;
        currentRunResult = null;
    }

    public void beforeCodeLineHook(String hookedClassQualifiedName,
            String hookedMethodSimpleName, String actualHookedMethodSimpleName,
            String hookedArgClassesStr, int hookedLine, int actualHookedLine) {
        if (currentRunResult == null) {
            return; // maybe called outside of the root method
        }

        // method containing this line
        String hookedMethodKey = TestMethod.generateMethodKey(
                hookedClassQualifiedName, hookedMethodSimpleName, hookedArgClassesStr);
        TestMethod hookedTestMethod;
        if (codeLineHookedMethodKeyCache != null
                && codeLineHookedMethodKeyCache.equals(hookedMethodKey)) {
            // re-use cache
            hookedTestMethod = codeLineHookedMethodCache;
        } else {
            try {
                hookedTestMethod = srcTree.getTestMethodByKey(hookedMethodKey, true);
            } catch (IllegalDataStructureException e) {
                throw new RuntimeException(e);
            }
        }

        // cache key and method to improve method search performance
        // caching is executed even if method is not found
        codeLineHookedMethodKeyCache = hookedMethodKey;
        codeLineHookedMethodCache = hookedTestMethod;

        if (hookedTestMethod == null) {
            return; // hooked method is not current root method
        }

        logger.info(String.format("beforeCodeBodyHook: start: %s(%d)", hookedMethodSimpleName, hookedLine));
        // TODO if groovy
        List<StackLine> stackLines = StackLineUtils.getStackLinesReplacingActual(
                srcTree, Thread.currentThread().getStackTrace(), hookedClassQualifiedName,
                actualHookedMethodSimpleName, hookedMethodSimpleName,
                actualHookedLine, hookedLine);
        if (stackLines.size() == 0) {
            throw new RuntimeException("implementation error");
        }

        CodeLine thisCodeLine = stackLines.get(0).getMethod().getCodeBody().get(
                stackLines.get(0).getCodeBodyIndex());
        CaptureStyle thisCaptureStyle;
        if (thisCodeLine.getCode() instanceof SubMethodInvoke) {
            SubMethodInvoke thisMethodInvoke = (SubMethodInvoke) thisCodeLine.getCode();
            thisCaptureStyle = thisMethodInvoke.getSubMethod().getCaptureStyle();
        } else if (thisCodeLine.getCode() instanceof VarAssign) {
            VarAssign assign = (VarAssign) thisCodeLine.getCode();
            if (assign.getValue() instanceof SubMethodInvoke) {
                SubMethodInvoke thisMethodInvoke = (SubMethodInvoke) assign.getValue();
                thisCaptureStyle = thisMethodInvoke.getSubMethod().getCaptureStyle();
            } else if (assign.getVariable() instanceof Field) {
                thisCaptureStyle = CaptureStyle.THIS_LINE;
            } else {
                logger.info("beforeCodeBodyHook: skip code: " + thisCodeLine.getCode().getOriginal());
                return;
            }
        } else if (thisCodeLine.getCode() instanceof TestStepLabel) {
            thisCaptureStyle = CaptureStyle.THIS_LINE;
        } else if (thisCodeLine.getCode() instanceof TestStep) {
            throw new RuntimeException("not supported");
        } else {
            logger.info("beforeCodeBodyHook: skip code: " + thisCodeLine.getCode().getOriginal());
            return;
        }
        if (thisCaptureStyle != CaptureStyle.THIS_LINE && thisCaptureStyle != CaptureStyle.STEP_IN) {
            logger.info("beforeCodeBodyHook: skip not capture line");
            return;
        }
        if (!canStepInCaptureTo(stackLines)) {
            logger.info("beforeCodeBodyHook: skip not stepInCapture line");
            return;
        }

        // screen capture
        File captureFile = captureScreen(currentRunResult.getRootMethod(), stackLines);
        if (captureFile != null) {
            logger.info("beforeCodeBodyHook: end with capture " + captureFile.getName());
        }
    }

    // - returns null if fails to capture
    private File captureScreenForStackLine(
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

    // returns null if not executed
    private File captureScreen(TestMethod rootMethod) {
        byte[] screenData = AdapterContainer.globalInstance().captureScreen();
        if (screenData == null) {
            return null;
        }

        File captureFile = new File(String.format("%s/%s/%s/%03d.png",
                captureRootDir, rootMethod.getTestClass().getQualifiedName(),
                rootMethod.getSimpleName(), currentCaptureNo));
        currentCaptureNo++;

        if (captureFile.getParentFile() != null) {
            captureFile.getParentFile().mkdirs();
        }
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

    // returns screen capture file
    // returns null if fail to capture
    private File captureScreen(TestMethod rootMethod, List<StackLine> stackLines) {
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
    private boolean canStepInCaptureTo(List<StackLine> stackLines) {
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
