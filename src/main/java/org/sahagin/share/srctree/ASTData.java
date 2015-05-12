package org.sahagin.share.srctree;

public class ASTData {
    // Memo data are not write to nor read from YAML data.
    // They are only temporal data mainly used for SrcTree generation
    private Object rawASTObjectMemo;
    private Object rawASTTypeMemo;

    public Object getRawASTObjectMemo() {
        return rawASTObjectMemo;
    }

    public void setRawASTObjectMemo(Object rawASTObjectMemo) {
        this.rawASTObjectMemo = rawASTObjectMemo;
    }

    public Object getRawASTTypeMemo() {
        return rawASTTypeMemo;
    }

    public void setRawASTTypeMemo(Object rawASTTypeMemo) {
        this.rawASTTypeMemo = rawASTTypeMemo;
    }

    protected final void clearMemo() {
        rawASTObjectMemo = null;
        rawASTTypeMemo = null;
    }

}
