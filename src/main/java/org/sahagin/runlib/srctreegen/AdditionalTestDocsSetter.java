package org.sahagin.runlib.srctreegen;

import org.sahagin.runlib.additionaltestdoc.AdditionalClassTestDoc;
import org.sahagin.runlib.additionaltestdoc.AdditionalMethodTestDoc;
import org.sahagin.runlib.additionaltestdoc.AdditionalPage;
import org.sahagin.runlib.additionaltestdoc.AdditionalTestDocs;
import org.sahagin.share.srctree.PageClass;
import org.sahagin.share.srctree.TestClass;
import org.sahagin.share.srctree.TestClassTable;
import org.sahagin.share.srctree.TestMethod;
import org.sahagin.share.srctree.TestMethodTable;

// If TestClass or TestMethod for the AdditionlTestDoc does not exist in the table,
// add them as sub TestClass or sub TestMethod.
// If already exists, override it's TestDoc value.
//
// - This class assumes class qualifiedName is unique
public class AdditionalTestDocsSetter {
    private TestClassTable rootClassTable;
    private TestClassTable subClassTable;
    private TestMethodTable rootMethodTable;
    private TestMethodTable subMethodTable;

    public AdditionalTestDocsSetter(TestClassTable rootClassTable, TestClassTable subClassTable,
            TestMethodTable rootMethodTable, TestMethodTable subMethodTable) {
        this.rootClassTable = rootClassTable;
        this.subClassTable = subClassTable;
        this.rootMethodTable = rootMethodTable;
        this.subMethodTable = subMethodTable;
    }

    public void set(AdditionalTestDocs testDocs) {
        // last set data in testDocs is used first

        for (int i = testDocs.getClassTestDocs().size() - 1; i >= 0; i--) {
            AdditionalClassTestDoc classTestDoc = testDocs.getClassTestDocs().get(i);
            setClass(classTestDoc.getQualifiedName(), classTestDoc.getTestDoc(),
                    classTestDoc instanceof AdditionalPage);
        }
        for (int i = testDocs.getMethodTestDocs().size() - 1; i >= 0; i--) {
            AdditionalMethodTestDoc testDoc = testDocs.getMethodTestDocs().get(i);
            setMethod(testDoc);
        }
    }

    // return the newly set TestClass instance or already set instance.
    private TestClass setClass(String qualifiedName, String testDoc, boolean isPage) {
        for (TestClass testClass : subClassTable.getTestClasses()) {
            // class qualified name must be unique
            if (qualifiedName.equals(testClass.getQualifiedName())) {
                return testClass;
            }
        }

        for (TestClass testClass : rootClassTable.getTestClasses()) {
            // class qualified name must be unique
            if (qualifiedName.equals(testClass.getQualifiedName())) {
                return testClass;
            }
        }

        TestClass newClass;
        if (isPage) {
            newClass = new PageClass();
        } else {
            newClass = new TestClass();
        }
        newClass.setKey(AdditionalClassTestDoc.generateClassKey(qualifiedName));
        newClass.setQualifiedName(qualifiedName);
        newClass.setTestDoc(testDoc);
        subClassTable.addTestClass(newClass);
        return newClass;
    }

    // return the newly set TestMethod instance or already set instance.
    private TestMethod setMethod(AdditionalMethodTestDoc testDoc) {
        // TODO consider about priority of multiple method additional TestDocs.
        // TODO when override non additional TestDoc by additional TestDoc
        String methodKey = AdditionalMethodTestDoc.generateMethodKey(testDoc);
        for (TestMethod testMethod : subMethodTable.getTestMethods()) {
            if (testMethod.getKey().equals(methodKey)) {
                return testMethod;
            }
        }
        for (TestMethod testMethod : rootMethodTable.getTestMethods()) {
            if (testMethod.getKey().equals(methodKey)) {
                return testMethod;
            }
        }

        TestMethod newMethod = new TestMethod();
        TestClass testClass = setClass(testDoc.getClassQualifiedName(), null, false);
        newMethod.setTestClassKey(testClass.getKey());
        newMethod.setTestClass(testClass);
        testClass.addTestMethod(newMethod);
        newMethod.setKey(methodKey);
        newMethod.setSimpleName(testDoc.getSimpleName());
        newMethod.setTestDoc(testDoc.getTestDoc());
        subMethodTable.addTestMethod(newMethod);
        return newMethod;
    }

}
