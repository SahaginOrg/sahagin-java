package org.sahagin.runlib.additionaltestdoc;

//class or interface
public class AdditionalClassTestDoc {
    private String qualifiedName;
    private String testDoc;
    // null represents no delegation
    private String delegateToQualifiedName = null;

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

    public String getDelegateToQualifiedName() {
        return delegateToQualifiedName;
    }

    public void setDelegateToQualifiedName(
            String delegateToQualifiedName) {
        this.delegateToQualifiedName = delegateToQualifiedName;
    }
}
