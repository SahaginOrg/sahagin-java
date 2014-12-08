package org.sahagin.share.additionaltestdoc;

// TODO cannot handle overloaded method
public class AdditionalFuncTestDoc {
    private String qualifiedName;
    private String testDoc;
    private boolean stepInCapture = false;

    public String getSimpleName() {
        if (qualifiedName == null) {
            return null;
        }
        int lastIndex = qualifiedName.lastIndexOf("."); // TODO name separator is always dot ??
        if (lastIndex == -1) {
            return qualifiedName;
        }
        return qualifiedName.substring(lastIndex + 1);
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public String getTestDoc() {
        return testDoc;
    }

    public void setTestDoc(String testDoc) {
        this.testDoc = testDoc;
    }

    public boolean isStepInCapture() {
        return stepInCapture;
    }

    public void setStepInCapture(boolean stepInCapture) {
        this.stepInCapture = stepInCapture;
    }

}
