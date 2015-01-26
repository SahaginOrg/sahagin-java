package org.sahagin.runlib.additionaltestdoc;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

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
        // last set data is referred first
        for (int i = methodTestDocs.size() - 1; i >= 0; i--) {
            AdditionalMethodTestDoc methodTestDoc = methodTestDocs.get(i);
            if (StringUtils.equals(methodTestDoc.getClassQualifiedName(), classQualifiedName)
                    && StringUtils.equals(methodTestDoc.getSimpleName(), methodSimpleName)) {
                if (!methodTestDoc.isOverloaded()) {
                    return methodTestDoc; // ignore method argument classes difference
                }

                // check method argument classes
                if (methodTestDoc.getArgClassQualifiedNames().size()
                        != argClassQualifiedNames.size()) {
                    continue;
                }
                boolean mismatchFound = false;
                for (int j = 0; j < methodTestDoc.getArgClassQualifiedNames().size(); j++) {
                    if (!StringUtils.equals(
                            methodTestDoc.getArgClassQualifiedNames().get(j), argClassQualifiedNames.get(j))) {
                        mismatchFound = true;
                        break;
                    }
                }

                if (mismatchFound) {
                    continue;
                }

                return methodTestDoc;
            }
        }
        return null;
    }

}
