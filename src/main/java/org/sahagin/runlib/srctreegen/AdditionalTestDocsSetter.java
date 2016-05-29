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
// This class does not resolve method invocation reference and delegate reference
// (so delegation destination TestClass may not be created yet)
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
        // override old data
        for (int i = 0; i < testDocs.getClassTestDocs().size(); i++) {
            AdditionalClassTestDoc classTestDoc = testDocs.getClassTestDocs().get(i);
            setClass(classTestDoc.getQualifiedName(), classTestDoc.getTestDoc(),
                    classTestDoc.getDelegateToQualifiedName(), classTestDoc instanceof AdditionalPage);
        }
        for (int i = 0; i < testDocs.getMethodTestDocs().size(); i++) {
            AdditionalMethodTestDoc testDoc = testDocs.getMethodTestDocs().get(i);
            setMethod(testDoc);
        }
    }

    private TestClass getTestClass(String qualifiedName) {
        if (qualifiedName == null) {
            return null;
        }

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
        return null;
    }

    // return the newly set TestClass instance or already set instance.
    private TestClass setClass(String qualifiedName, String testDoc,
            String delegateToClassQualifiedName, boolean isPage) {
        TestClass testClass = getTestClass(qualifiedName);
        if (testClass != null) {
            // override existing testDoc ( don't override other information)
            testClass.setTestDoc(testDoc);
            return testClass;
        }

        TestClass newClass;
        if (isPage) {
            newClass = new PageClass();
        } else {
            newClass = new TestClass();
        }
        newClass.setKey(qualifiedName);
        newClass.setQualifiedName(qualifiedName);
        newClass.setTestDoc(testDoc);
        newClass.setDelegateToTestClassKey(delegateToClassQualifiedName);
        // class without root method and sub method is regarded as sub class
        subClassTable.addTestClass(newClass);
        return newClass;
    }

    // return the newly set TestMethod instance or already set instance.
    // - TestDoc of the same name and argument types method can be overridden
    private TestMethod setMethod(AdditionalMethodTestDoc testDoc) {
        String methodKey;
        if (testDoc.isOverloaded()) {
            methodKey = TestMethod.generateMethodKey(
                    testDoc.getClassQualifiedName(), testDoc.getSimpleName(), testDoc.getArgClassesStr());
        } else {
            methodKey = TestMethod.generateMethodKey(
                    testDoc.getClassQualifiedName(), testDoc.getSimpleName());
        }
        for (TestMethod testMethod : subMethodTable.getTestMethods()) {
            if (testMethod.getKey().equals(methodKey)) {
                // override existing testDoc ( don't override other information)
                testMethod.setTestDoc(testDoc.getTestDoc());
                return testMethod;
            }
        }
        for (TestMethod testMethod : rootMethodTable.getTestMethods()) {
            if (testMethod.getKey().equals(methodKey)) {
                // override existing testDoc ( don't override other information)
                testMethod.setTestDoc(testDoc.getTestDoc());
                return testMethod;
            }
        }

        TestMethod newMethod = new TestMethod();
        TestClass testClass = setClass(testDoc.getClassQualifiedName(), null, null, false);
        newMethod.setTestClassKey(testClass.getKey());
        newMethod.setTestClass(testClass);
        testClass.addTestMethod(newMethod);
        newMethod.setKey(methodKey);
        newMethod.setSimpleName(testDoc.getSimpleName());
        newMethod.setTestDoc(testDoc.getTestDoc());
        newMethod.setCaptureStyle(testDoc.getCaptureStyle());
        newMethod.setVariableLengthArgIndex(testDoc.getVariableLengthArgIndex());
        subMethodTable.addTestMethod(newMethod);
        return newMethod;
    }
}
