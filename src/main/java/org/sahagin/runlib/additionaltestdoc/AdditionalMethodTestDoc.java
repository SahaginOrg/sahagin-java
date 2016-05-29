package org.sahagin.runlib.additionaltestdoc;

import org.sahagin.runlib.external.CaptureStyle;

public class AdditionalMethodTestDoc {
    private String classQualifiedName;
    private String simpleName;
    // null means not overloaded
    private String argClassesStr = null;
    private int variableLengthArgIndex = -1;
    private String testDoc;
    // TODO should not allow stepInCapture for AdditionalTestDoc
    private CaptureStyle captureStyle = CaptureStyle.getDefault();

    public String getClassQualifiedName() {
        return classQualifiedName;
    }

    public void setClassQualifiedName(String classQualifiedName) {
        this.classQualifiedName = classQualifiedName;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    public String getQualifiedName() {
        if (classQualifiedName == null || simpleName == null) {
            return simpleName;
        } else {
            return classQualifiedName + "." + simpleName;
        }
    }

    public boolean isOverloaded() {
        return argClassesStr != null;
    }

    public String getArgClassesStr() {
        if (!isOverloaded()) {
            throw new IllegalStateException(
                    "not overloaded method, and argument information is not set");
        }
        return argClassesStr;
    }

    public void setNotOverload() {
        argClassesStr = null;
    }

    public void setOverload(String argClassesStr) {
        if (argClassesStr == null) {
            throw new NullPointerException();
        }
        this.argClassesStr = argClassesStr;
    }

    public boolean hasVariableLengthArg() {
        return variableLengthArgIndex != -1;
    }

    public int getVariableLengthArgIndex() {
        return variableLengthArgIndex;
    }

    public void setVariableLengthArgIndex(int variableLengthArgIndex) {
        this.variableLengthArgIndex = variableLengthArgIndex;
    }

    public String getTestDoc() {
        return testDoc;
    }

    public void setTestDoc(String testDoc) {
        this.testDoc = testDoc;
    }

    public CaptureStyle getCaptureStyle() {
        return captureStyle;
    }

    public void setCaptureStyle(CaptureStyle captureStyle) {
        this.captureStyle = captureStyle;
    }
}
