package org.sahagin.runlib.additionaltestdoc;

import java.util.ArrayList;
import java.util.List;

public class AdditionalTestDocs {
    private List<AdditionalClassTestDoc> classTestDocs
    = new ArrayList<AdditionalClassTestDoc>(128);
    private List<AdditionalMethodTestDoc> methodTestDocs
    = new ArrayList<AdditionalMethodTestDoc>(256);

    public List<AdditionalClassTestDoc> getClassTestDocs() {
        return classTestDocs;
    }

    public void classAdd(AdditionalClassTestDoc classTestDoc) {
        classTestDocs.add(classTestDoc);
    }

    public void classAdd(String qualifiedName, String testDoc) {
        AdditionalClassTestDoc additionalClassTestDoc = new AdditionalClassTestDoc();
        additionalClassTestDoc.setQualifiedName(qualifiedName);
        additionalClassTestDoc.setTestDoc(testDoc);
        classTestDocs.add(additionalClassTestDoc);
    }

    public List<AdditionalMethodTestDoc> getMethodTestDocs() {
        return methodTestDocs;
    }

    public void methodAdd(AdditionalMethodTestDoc methodTestDoc) {
        methodTestDocs.add(methodTestDoc);
    }

    public void methodAdd(String classQualifiedName, String simpleName, String testDoc) {
        AdditionalMethodTestDoc additionalMethodTestDoc = new AdditionalMethodTestDoc();
        additionalMethodTestDoc.setClassQualifiedName(classQualifiedName);
        additionalMethodTestDoc.setQualifiedName(classQualifiedName + "." + simpleName);
        additionalMethodTestDoc.setTestDoc(testDoc);
        methodTestDocs.add(additionalMethodTestDoc);
    }

    // returns null if not found
    public AdditionalClassTestDoc getClassTestDoc(String qualifiedClassName) {
        if (qualifiedClassName == null) {
            throw new NullPointerException();
        }
        // last set data is referred first
        for (int i = classTestDocs.size() - 1; i >= 0; i--) {
            AdditionalClassTestDoc classTestDoc = classTestDocs.get(i);
            if (qualifiedClassName.equals(classTestDoc.getQualifiedName())) {
                return classTestDoc;
            }
        }
        return null;
    }

    // returns null if not found
    public AdditionalMethodTestDoc getMethodTestDoc(String qualifiedMethodName) {
        if (qualifiedMethodName == null) {
            throw new NullPointerException();
        }
        // last set data is referred first
        for (int i = methodTestDocs.size() - 1; i >= 0; i--) {
            AdditionalMethodTestDoc methodTestDoc = methodTestDocs.get(i);
            if (qualifiedMethodName.equals(methodTestDoc.getQualifiedName())) {
                return methodTestDoc;
            }
        }
        return null;
    }

}
