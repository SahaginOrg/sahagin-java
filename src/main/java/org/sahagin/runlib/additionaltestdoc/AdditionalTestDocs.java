package org.sahagin.runlib.additionaltestdoc;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sahagin.share.srctree.TestMethod;

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

    public List<AdditionalMethodTestDoc> getMethodTestDocs() {
        return methodTestDocs;
    }

    public void methodAdd(AdditionalMethodTestDoc methodTestDoc) {
        methodTestDocs.add(methodTestDoc);
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
    // TODO delegation should work for not additional TestDoc
    public AdditionalMethodTestDoc getMethodTestDoc(String classQualifiedName,
            String methodSimpleName, List<String> argClassQualifiedNames) {
        if (classQualifiedName == null) {
            throw new NullPointerException();
        }
        if (methodSimpleName == null) {
            throw new NullPointerException();
        }
        if (argClassQualifiedNames == null) {
            throw new NullPointerException();
        }
        for (int i = 0; i < methodTestDocs.size(); i++) {
            AdditionalMethodTestDoc methodTestDoc = methodTestDocs.get(i);
            String targetClassQualifiedName = classQualifiedName;
            while (targetClassQualifiedName != null) {
                if (matchesToMethodTestDoc(methodTestDoc,
                        targetClassQualifiedName, methodSimpleName, argClassQualifiedNames)) {
                    return methodTestDoc;
                }
                // TODO delegation should work for not additional TestDoc
                AdditionalClassTestDoc classTestDoc = getClassTestDoc(targetClassQualifiedName);
                if (classTestDoc == null) {
                    break;
                }
                targetClassQualifiedName = classTestDoc.getDelegateToQualifiedName();
            }
        }
        return null;
    }

    private boolean matchesToMethodTestDoc(AdditionalMethodTestDoc methodTestDoc,
            String classQualifiedName, String methodSimpleName, List<String> argClassQualifiedNames) {
        if (StringUtils.equals(methodTestDoc.getClassQualifiedName(), classQualifiedName)
                && StringUtils.equals(methodTestDoc.getSimpleName(), methodSimpleName)) {
            if (!methodTestDoc.isOverloaded()) {
                return true; // ignore method argument classes difference
            }

            String methodKey = TestMethod.generateMethodKey(
                    classQualifiedName, methodSimpleName, argClassQualifiedNames);
            String testDocMethodKey = TestMethod.generateMethodKey(
                    methodTestDoc.getClassQualifiedName(),
                    methodTestDoc.getSimpleName(), methodTestDoc.getArgClassesStr());
            if (methodKey.equals(testDocMethodKey)) {
                return true;
            }
        }
        return false;
    }

    public void clear() {
        classTestDocs.clear();
        methodTestDocs.clear();
    }

}
