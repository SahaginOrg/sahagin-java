package org.sahagin.report;

import java.util.ArrayList;
import java.util.List;

import org.sahagin.share.runresults.StackLine;
import org.sahagin.share.srctree.code.CodeLine;
import org.sahagin.share.srctree.code.SubMethodInvoke;

/**
 * CodeLine for report
 */
public class ReportCodeLine {
    private CodeLine codeLine;
    private String pagetTestDoc;
    // placeholder must have been resolved
    private String testDoc;
    // placeholder must have been resolved
    private List<String> methodArgTestDocs = new ArrayList<String>(4);
    private List<StackLine> stackLines;
    private boolean hasError = false;
    private boolean alreadyRun = false;
    private String ttId;
    private String parentTtId = null; // null means no parent

    public String getMethodKey() {
        if (!(codeLine.getCode() instanceof SubMethodInvoke)) {
            return "";
        }

        SubMethodInvoke invoke = (SubMethodInvoke) codeLine.getCode();
        if (invoke.isChildInvoke()) {
            // don't add child HTML report for childInvoke
            // since the code body for the sub method is not the code body of
            // the actually invoked method.
            // TODO consider about this behavior
            return "";
        }
        return invoke.getSubMethodKey();
    }

    public String getOriginal() {
        return codeLine.getCode().getOriginal();
    }

    public CodeLine getCodeLine() {
        return this.codeLine;
    }

    public void setCodeLine(CodeLine codeLine) {
        this.codeLine = codeLine;
    }

    public String getPageTestDoc() {
        return pagetTestDoc;
    }

    public void setPagetTestDoc(String pagetTestDoc) {
        this.pagetTestDoc = pagetTestDoc;
    }

    public String getTestDoc() {
        return testDoc;
    }

    public void setTestDoc(String testDoc) {
        this.testDoc = testDoc;
    }

    public List<String> getMethodArgTestDocs() {
        return methodArgTestDocs;
    }

    public void addMethodArgTestDoc(String methodArgTestDoc) {
        this.methodArgTestDocs.add(methodArgTestDoc);
    }

    public void addAllMethodArgTestDocs(List<String> methodArgTestDocs) {
        for (String methodArgTestDoc : methodArgTestDocs) {
            this.methodArgTestDocs.add(methodArgTestDoc);
        }
    }

    public List<StackLine> getStackLines() {
        return stackLines;
    }

    public void setStackLines(List<StackLine> stackLines) {
        this.stackLines = stackLines;
    }

    public boolean hasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }

    public boolean isAlreadyRun() {
        return alreadyRun;
    }

    public void setAlreadyRun(boolean alreadyRun) {
        this.alreadyRun = alreadyRun;
    }

    public String getTtId() {
        return ttId;
    }

    public void setTtId(String ttId) {
        this.ttId = ttId;
    }

    public String getParentTtId() {
        return parentTtId;
    }

    public void setParentTtId(String parentTtId) {
        this.parentTtId = parentTtId;
    }

}
