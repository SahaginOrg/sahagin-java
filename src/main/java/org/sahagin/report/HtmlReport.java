package org.sahagin.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.openqa.selenium.io.IOUtils;
import org.sahagin.share.CommonPath;
import org.sahagin.share.CommonUtils;
import org.sahagin.share.IllegalDataStructureException;
import org.sahagin.share.IllegalTestScriptException;
import org.sahagin.share.Logging;
import org.sahagin.share.TestDocResolver;
import org.sahagin.share.runresults.LineScreenCapture;
import org.sahagin.share.runresults.RootFuncRunResult;
import org.sahagin.share.runresults.RunFailure;
import org.sahagin.share.runresults.RunResults;
import org.sahagin.share.runresults.StackLine;
import org.sahagin.share.srctree.SrcTree;
import org.sahagin.share.srctree.TestFunction;
import org.sahagin.share.srctree.TestMethod;
import org.sahagin.share.srctree.code.CodeLine;
import org.sahagin.share.srctree.code.SubFunctionInvoke;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlUtils;

//TODO support TestDoc annotation for Enumerate type
//TODO support method optional argument

public class HtmlReport {
    private static Logger logger = Logging.getLogger(HtmlReport.class.getName());

    public HtmlReport() {
        // stop generating velocity.log
        Velocity.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS,
                "org.apache.velocity.runtime.log.NullLogSystem");
        Velocity.init();
    }

    private String generateTtId(List<StackLine> stackLines) {
        if (stackLines.size() == 0) {
            throw new IllegalArgumentException("empty stackLines");
        }
        String ttId = "";
        for (int i = stackLines.size() - 1; i >= 0; i--) {
            if (i != stackLines.size() - 1) {
                ttId = ttId + "_";
            }
            ttId = ttId + Integer.toString(stackLines.get(i).getCodeBodyIndex());
        }
        return ttId;
    }

    // generate ResportScreenCapture list from lineScreenCaptures and
    private List<ReportScreenCapture> generateReportScreenCaptures(
            List<LineScreenCapture> lineScreenCaptures,
            File inputCaptureRootDir, File reportOutputDir, File funcReportParentDir) {
        List<ReportScreenCapture> reportCaptures
        = new ArrayList<ReportScreenCapture>(lineScreenCaptures.size());

        // add noImage capture
        String noImageFilePath = new File(CommonUtils.relativize(
                CommonPath.htmlExternalResourceRootDir(reportOutputDir), funcReportParentDir),
                "images/noImage.png").getPath();
        ReportScreenCapture noImageCapture = new ReportScreenCapture();
        noImageCapture.setPath(noImageFilePath);
        noImageCapture.setTtId("noImage");
        reportCaptures.add(noImageCapture);

        logger.info("inputCaptureRootDir: " + inputCaptureRootDir);
        logger.info("reportOutputDir: " + reportOutputDir);
        logger.info("funcReportParentDir: " + funcReportParentDir);

        // add each line screen capture
        for (LineScreenCapture lineScreenCapture : lineScreenCaptures) {
            ReportScreenCapture reportCapture = new ReportScreenCapture();
            File relInputCapturePath = CommonUtils.relativize(
                    lineScreenCapture.getPath(), inputCaptureRootDir);
            File absOutputCapturePath = new File(
                    CommonPath.htmlReportCaptureRootDir(reportOutputDir), relInputCapturePath.getPath());
            File relOutputCapturePath = CommonUtils.relativize(absOutputCapturePath, funcReportParentDir);
            reportCapture.setPath(relOutputCapturePath.getPath());
            String ttId = generateTtId(lineScreenCapture.getStackLines());
            reportCapture.setTtId(ttId);
            reportCaptures.add(reportCapture);
        }
        return reportCaptures;
    }

    // returns null if no failure
    private RunFailure getRunFailure(RootFuncRunResult runResult) {
        if (runResult == null || runResult.getRunFailures().size() == 0) {
            return null; // no failure
        }

        // multiple run failures in one test method are not supported yet
        return runResult.getRunFailures().get(0);
    }

    // returns 0 if stackLines == errStackLines (means error line),
    // returns positive if stackLines > errStackLines (means not executed),
    // returns negative if stackLines < errStackLines (means already executed)
    private int compareToErrStackLines(List<StackLine> stackLines, List<StackLine> errStackLines) {
        if (errStackLines == null || errStackLines.size() == 0) {
            return -1; // already executed
        }
        if (stackLines.size() == 0) {
            throw new IllegalArgumentException("empty stackLine");
        }

        for (int i = 0; i < stackLines.size(); i++) {
            if (i >= errStackLines.size()) {
                throw new IllegalArgumentException("stackLines and errStackLines mismatch");
            }
            int indexFromTail = stackLines.size() - 1 - i;
            int errIndexFromTail = errStackLines.size() - 1 - i;
            int line = stackLines.get(indexFromTail).getCodeBodyIndex();
            int errLine = errStackLines.get(errIndexFromTail).getCodeBodyIndex();
            if (line < errLine) {
                return -1; // already executed
            } else if (line > errLine) {
                return 1; // not executed
            }
        }
        return 0;
    }

    private String pageTestDoc(CodeLine codeLine) {
        String pageTestDoc = TestDocResolver.pageTestDoc(codeLine.getCode());
        if (pageTestDoc == null || pageTestDoc.equals("")) {
            return "-";
        } else {
            return pageTestDoc;
        }
    }

    private String placeholderResolvedTestDoc(CodeLine codeLine, List<String> parentFuncArgTestDocs)
            throws IllegalTestScriptException {
        String funcTestDoc = TestDocResolver.placeholderResolvedFuncTestDoc(
                codeLine.getCode(), parentFuncArgTestDocs);
        if (funcTestDoc == null) {
            return "";
        }
        return funcTestDoc;
    }

    private ReportCodeLine generateReportCodeLine(CodeLine codeLine, List<String> parentFuncArgTestDocs,
            List<StackLine> stackLines, RunFailure runFailure, String ttId, String parentTtId)
                    throws IllegalTestScriptException {
        if (parentFuncArgTestDocs == null) {
            throw new NullPointerException();
        }
        if (stackLines == null) {
            throw new NullPointerException();
        }
        ReportCodeLine result = new ReportCodeLine();
        result.setCodeLine(codeLine);
        String pageTestDoc = pageTestDoc(codeLine);
        result.setPagetTestDoc(pageTestDoc);
        String testDoc = placeholderResolvedTestDoc(codeLine, parentFuncArgTestDocs);
        result.setTestDoc(testDoc);
        List<String> funcArgTestDocs
        = TestDocResolver.placeholderResolvedFuncArgTestDocs(codeLine.getCode(), parentFuncArgTestDocs);
        result.addAllFuncArgTestDocs(funcArgTestDocs);
        result.setStackLines(stackLines);
        result.setTtId(ttId);
        result.setParentTtId(parentTtId);
        List<StackLine> errStackLines = null;
        if (runFailure != null) {
            errStackLines = runFailure.getStackLines();
        }
        int errCompare = compareToErrStackLines(stackLines, errStackLines);
        if (errCompare < 0) {
            result.setHasError(false);
            result.setAlreadyRun(true);
        } else if (errCompare == 0) {
            result.setHasError(true);
            result.setAlreadyRun(true);
        } else if (errCompare > 0) {
            result.setHasError(false);
            result.setAlreadyRun(false);
        } else {
            throw new RuntimeException("implementation error");
        }
        return result;
    }

    private StackLine generateStackLine(TestFunction function, String functionKey,
            int codeBodyIndex, int line) {
        StackLine result = new StackLine();
        result.setFunction(function);
        result.setFunctionKey(functionKey);
        result.setCodeBodyIndex(codeBodyIndex);
        result.setLine(line);
        return result;
    }

    // runFailure... set null if not error
    private List<ReportCodeLine> generateReportCodeBody(
            TestFunction rootFunction, RunFailure runFailure) throws IllegalTestScriptException {
        List<ReportCodeLine> result = new ArrayList<ReportCodeLine>(rootFunction.getCodeBody().size());
        for (int i = 0; i < rootFunction.getCodeBody().size(); i++) {
            CodeLine codeLine = rootFunction.getCodeBody().get(i);
            String rootTtId = Integer.toString(i);

            StackLine rootStackLine = generateStackLine(
                    rootFunction, rootFunction.getKey(), i, codeLine.getStartLine());
            List<StackLine> rootStackLines = new ArrayList<StackLine>(1);
            rootStackLines.add(rootStackLine);

            ReportCodeLine reportCodeLine = generateReportCodeLine(
                    codeLine, new ArrayList<String>(0), rootStackLines, runFailure, rootTtId, null);
            result.add(reportCodeLine);

            // add direct child to HTML report
            if (codeLine.getCode() instanceof SubFunctionInvoke) {
                SubFunctionInvoke invoke = (SubFunctionInvoke) codeLine.getCode();
                List<String> parentFuncArgTestDocs = reportCodeLine.getFuncArgTestDocs();
                List<CodeLine> codeBody = invoke.getSubFunction().getCodeBody();
                for (int j = 0; j < codeBody.size(); j++) {
                    CodeLine childCodeLine = codeBody.get(j);

                    StackLine childStackLine = generateStackLine(invoke.getSubFunction(),
                            invoke.getSubFunctionKey(), j, childCodeLine.getStartLine());
                    List<StackLine> childStackLines = new ArrayList<StackLine>(2);
                    childStackLines.add(childStackLine);
                    childStackLines.add(rootStackLine);

                    ReportCodeLine childReportCodeLine = generateReportCodeLine(
                            childCodeLine, parentFuncArgTestDocs, childStackLines,
                            runFailure, rootTtId + "_" + j, rootTtId);
                    result.add(childReportCodeLine);
                }
            }
        }
        return result;
    }

    private SrcTree generateSrcTree(File reportInputDataDir)
            throws IllegalDataStructureException {
        // generate srcTree from YAML file
        Map<String, Object> yamlObj = YamlUtils.load(
                CommonPath.srcTreeFile(reportInputDataDir));
        SrcTree srcTree = new SrcTree();
        try {
            srcTree.fromYamlObject(yamlObj);
        } catch (YamlConvertException e) {
            throw new IllegalDataStructureException(e);
        }
        srcTree.resolveKeyReference();
        return srcTree;
    }

    private RunResults generateRunResults(File reportInputDataDir, SrcTree srcTree)
            throws IllegalDataStructureException {
        RunResults results = new RunResults();
        Collection<File> runResultFiles;
        File runResultsRootDir = CommonPath.runResultRootDir(reportInputDataDir);
        if (runResultsRootDir.exists()) {
            runResultFiles = FileUtils.listFiles(runResultsRootDir, null, true);
        } else {
            runResultFiles = new ArrayList<File>(0);
        }
        for (File runResultFile : runResultFiles) {
            Map<String, Object> runResultYamlObj = YamlUtils.load(runResultFile);
            RootFuncRunResult rootFuncRunResult = new RootFuncRunResult();
            try {
                rootFuncRunResult.fromYamlObject(runResultYamlObj);
            } catch (YamlConvertException e) {
                throw new IllegalDataStructureException(e);
            }
            results.addRootFuncRunResults(rootFuncRunResult);
        }
        results.resolveKeyReference(srcTree);
        return results;
    }

    private void deleteDirIfExists(File dir) {
        if (dir.exists()) {
            try {
                FileUtils.deleteDirectory(dir);
            } catch (IOException e) {
                throw new RuntimeException("fail to delete " + dir.getAbsolutePath(), e);
            }
        }
    }

    private void escapePut(VelocityContext context, String key, String value) {
        context.put(key, StringEscapeUtils.escapeHtml(value));
    }

    // each report HTML file is {methodQualifiedParentPath}/{methodSimpleName}.html
    public void generate(File reportInputDataDir, File reportOutputDir)
            throws IllegalDataStructureException, IllegalTestScriptException {
        deleteDirIfExists(reportOutputDir); // delete previous execution output
        SrcTree srcTree = generateSrcTree(reportInputDataDir);
        RunResults runResults = generateRunResults(reportInputDataDir, srcTree);

        File htmlExternalResRootDir = CommonPath.htmlExternalResourceRootDir(reportOutputDir);

        // generate src-tree-yaml.js
        String srcTreeYamlStr;
        try {
            srcTreeYamlStr = FileUtils.readFileToString(CommonPath.srcTreeFile(reportInputDataDir), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        VelocityContext srcTreeContext = new VelocityContext();
        // don't need HTML encode
        srcTreeContext.put("yamlStr", srcTreeYamlStr);
        File srcTreeYamlJsFile = new File(htmlExternalResRootDir, "js/report/src-tree-yaml.js");
        generateVelocityOutput(srcTreeContext, "/template/src-tree-yaml.js.vm", srcTreeYamlJsFile);

        // set up HTML external files
        // TODO all file paths are hard coded. this is very poor logic..
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "js/share/common-utils.js");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "js/share/capture-style.js");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "js/share/yaml/js-yaml.min.js");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "js/share/yaml/yaml-utils.js");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "js/share/srctree/code/code.js");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "js/share/srctree/code/string-code.js");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "js/share/srctree/code/func-argument.js");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "js/share/srctree/code/unknown-code.js");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "js/share/srctree/code/sub-function-invoke.js");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "js/share/srctree/code/sub-method-invoke.js");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "js/share/srctree/code/code-line.js");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "js/share/srctree/test-class.js");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "js/share/srctree/page-class.js");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "js/share/srctree/test-function.js");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "js/share/srctree/test-method.js");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "js/share/srctree/test-func-table.js");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "js/share/srctree/test-class-table.js");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "js/share/srctree/src-tree.js");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "js/share/testdoc-resolver.js");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "css/fonts/flexslider-icon.eot");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "css/fonts/flexslider-icon.svg");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "css/fonts/flexslider-icon.ttf");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "css/fonts/flexslider-icon.woff");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "css/images/bx_loader.gif");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "css/images/controls.png");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "css/jquery.bxslider.css");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "css/jquery.treetable.css");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "css/jquery.treetable.theme.default.css");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "css/perfect-scrollbar.min.css");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "css/report.css");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "images/noImage.png");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "js/report/jquery-1.11.1.min.js");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "js/report/jquery.bxslider.min.js");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "js/report/jquery.treetable.js");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "js/report/perfect-scrollbar.min.js");
        extractHtmlExternalResFromThisJar(htmlExternalResRootDir, "js/report/report.js");

        // copy screen captures to reportOutputDir
        // TODO copying screen capture may be slow action
        File inputCaptureRootDir = CommonPath.inputCaptureRootDir(reportInputDataDir);
        File htmlReportCaptureRootDir = CommonPath.htmlReportCaptureRootDir(reportOutputDir);
        try {
            if (inputCaptureRootDir.exists()) {
                FileUtils.copyDirectory(inputCaptureRootDir, htmlReportCaptureRootDir);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<TestFunction> testFunctions = srcTree.getRootFuncTable().getTestFunctions();
        File reportMainDir = CommonPath.htmlReportMainFile(reportOutputDir).getParentFile();
        List<ReportFuncLink> reportLinks = new ArrayList<ReportFuncLink>(testFunctions.size());

        // generate each function report
        for (TestFunction rootFunc : testFunctions) {
            TestMethod method = (TestMethod) rootFunc;
            File funcReportParentDir = new File(CommonPath.funcHtmlReportRootDir(reportOutputDir),
                    method.getTestClass().getQualifiedName());
            funcReportParentDir.mkdirs();

            VelocityContext funcContext = new VelocityContext();
            if (rootFunc.getTestDoc() == null) {
                escapePut(funcContext, "title", rootFunc.getSimpleName());
            } else {
                escapePut(funcContext, "title", rootFunc.getTestDoc());
            }

            escapePut(funcContext, "externalResourceRootDir", CommonUtils.relativize(
                    CommonPath.htmlExternalResourceRootDir(reportOutputDir), funcReportParentDir).getPath());
            if (!(rootFunc instanceof TestMethod)) {
                throw new RuntimeException("not supported yet: " + rootFunc);
            }

            escapePut(funcContext, "className", method.getTestClass().getQualifiedName());
            escapePut(funcContext, "classTestDoc", method.getTestClass().getTestDoc());
            escapePut(funcContext, "funcName", rootFunc.getSimpleName());
            escapePut(funcContext, "funcTestDoc", rootFunc.getTestDoc());

            RootFuncRunResult runResult = runResults.getRunResultByRootFunction(rootFunc);
            RunFailure runFailure = getRunFailure(runResult);
            if (runFailure == null) {
                escapePut(funcContext, "errMsg", null);
                escapePut(funcContext, "errLineTtId", "");
            } else {
                escapePut(funcContext, "errMsg", runFailure.getMessage().trim());
                escapePut(funcContext, "errLineTtId", generateTtId(runFailure.getStackLines()));
            }

            List<ReportCodeLine> reportCodeBody = generateReportCodeBody(rootFunc, runFailure);
            funcContext.put("codeBody", reportCodeBody);

            List<LineScreenCapture> lineScreenCaptures;
            if (runResult == null) {
                lineScreenCaptures = new ArrayList<LineScreenCapture>(0);
            } else {
                lineScreenCaptures = runResult.getLineScreenCaptures();
            }
            List<ReportScreenCapture> captures = generateReportScreenCaptures(
                    lineScreenCaptures, inputCaptureRootDir, reportOutputDir, funcReportParentDir);
            funcContext.put("captures", captures);

            File funcReportFile = new File(funcReportParentDir, rootFunc.getSimpleName() + ".html");
            generateVelocityOutput(funcContext, "/template/report.html.vm", funcReportFile);

            // set reportLinks data
            ReportFuncLink reportLink = new ReportFuncLink();
            reportLink.setTitle(method.getQualifiedName());
            reportLink.setPath(CommonUtils.relativize(funcReportFile, reportMainDir).getPath());
            reportLinks.add(reportLink);
        }
        // TODO HTML encode all codeBody, captures, reportLinks values

        // generate main index.html report
        VelocityContext mainContext = new VelocityContext();
        mainContext.put("reportLinks", reportLinks);
        generateVelocityOutput(mainContext, "/template/index.html.vm",
                CommonPath.htmlReportMainFile(reportOutputDir));
    }

    private void generateVelocityOutput(
            VelocityContext context, String templateResourcePath, File outputFile) {
        outputFile.getParentFile().mkdirs();
        InputStream in = null;
        Reader reader = null;
        FileWriterWithEncoding writer = null;
        try {
            in = this.getClass().getResourceAsStream(templateResourcePath);
            reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            writer = new FileWriterWithEncoding(outputFile, "UTF-8");
            Velocity.evaluate(context, writer, this.getClass().getSimpleName(), reader);
            writer.close();
            reader.close();
            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(in);
        }
    }

    private void extractHtmlExternalResFromThisJar(File htmlExternalResourceRootDir, String copyPath) {
        InputStream in = this.getClass().getResourceAsStream("/" + copyPath);
        File destFile = new File(htmlExternalResourceRootDir, copyPath);
        destFile.getParentFile().mkdirs();
        try {
            FileUtils.copyInputStreamToFile(in, destFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
