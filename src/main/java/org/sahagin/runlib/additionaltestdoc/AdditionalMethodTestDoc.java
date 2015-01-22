package org.sahagin.runlib.additionaltestdoc;

import org.sahagin.runlib.external.CaptureStyle;

// TODO cannot handle overloaded method
public class AdditionalMethodTestDoc {
    private String classQualifiedName;
    private String simpleName;
    private String testDoc;
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
