package org.sahagin.runlib.additionaltestdoc;

import java.util.ArrayList;
import java.util.List;

public class AdditionalTestDocs {
    private List<AdditionalClassTestDoc> classTestDocs
    = new ArrayList<AdditionalClassTestDoc>(128);
    private List<AdditionalFuncTestDoc> funcTestDocs
    = new ArrayList<AdditionalFuncTestDoc>(256);

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

    public List<AdditionalFuncTestDoc> getFuncTestDocs() {
        return funcTestDocs;
    }

    public void funcAdd(AdditionalFuncTestDoc funcTestDoc) {
        funcTestDocs.add(funcTestDoc);
    }

    public void methodAdd(String classQualifiedName, String simpleName, String testDoc) {
        AdditionalMethodTestDoc additionalMethodTestDoc = new AdditionalMethodTestDoc();
        additionalMethodTestDoc.setClassQualifiedName(classQualifiedName);
        additionalMethodTestDoc.setQualifiedName(classQualifiedName + "." + simpleName);
        additionalMethodTestDoc.setTestDoc(testDoc);
        funcTestDocs.add(additionalMethodTestDoc);
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
    public AdditionalFuncTestDoc getFuncTestDoc(String qualifiedFuncName) {
        if (qualifiedFuncName == null) {
            throw new NullPointerException();
        }
        // last set data is referred first
        for (int i = funcTestDocs.size() - 1; i >= 0; i--) {
            AdditionalFuncTestDoc funcTestDoc = funcTestDocs.get(i);
            if (qualifiedFuncName.equals(funcTestDoc.getQualifiedName())) {
                return funcTestDoc;
            }
        }
        return null;
    }

}
