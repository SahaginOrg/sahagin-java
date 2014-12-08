package org.sahagin.report;

import java.util.List;

import org.sahagin.share.IllegalTestScriptException;
import org.sahagin.share.TestDocResolver;
import org.sahagin.share.runresults.StackLine;
import org.sahagin.share.srctree.code.Code;
import org.sahagin.share.srctree.code.CodeLine;
import org.sahagin.share.srctree.code.SubFunctionInvoke;

/**
 * CodeLine for report
 */
public class ReportCodeLine {
    private CodeLine codeLine;
    private List<StackLine> stackLines;
    private boolean hasError = false;
    private boolean alreadyRun = false;
    private String ttId;
    private String parentTtId = null; // null means no parent

    public String getFunctionKey() {
        if (!(codeLine.getCode() instanceof SubFunctionInvoke)) {
            return "";
        }
        SubFunctionInvoke invoke = (SubFunctionInvoke) codeLine.getCode();
        return invoke.getSubFunctionKey();
    }

    public String getPageTestDoc() {
        String pageTestDoc = TestDocResolver.pageTestDoc(codeLine.getCode());
        if (pageTestDoc == null || pageTestDoc.equals("")) {
            return "-";
        } else {
            return pageTestDoc;
        }
    }

    public String getPlaceholderResolvedTestDoc() throws IllegalTestScriptException {
        Code code = codeLine.getCode();
        String funcTestDoc = TestDocResolver.placeholderResolvedFuncTestDoc(code);
        if (funcTestDoc == null) {
            return "";
        }
        return funcTestDoc;
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
