package org.sahagin.runlib.srctreegen;

import org.sahagin.runlib.additionaltestdoc.AdditionalClassTestDoc;
import org.sahagin.runlib.additionaltestdoc.AdditionalFuncTestDoc;
import org.sahagin.runlib.additionaltestdoc.AdditionalMethodTestDoc;
import org.sahagin.runlib.additionaltestdoc.AdditionalPage;
import org.sahagin.runlib.additionaltestdoc.AdditionalTestDocs;
import org.sahagin.share.srctree.PageClass;
import org.sahagin.share.srctree.TestClass;
import org.sahagin.share.srctree.TestClassTable;
import org.sahagin.share.srctree.TestFuncTable;
import org.sahagin.share.srctree.TestFunction;
import org.sahagin.share.srctree.TestMethod;

// If TestClass or TestFunction for the AdditionlTestDoc does not exist in the table,
// add them as sub TestClass or sub TestFunction.
// If already exists, override it's TestDoc value.
//
// - This class assumes class qualifiedName is unique
// - This class does not support method overload yet..
public class AdditionalTestDocsSetter {
    private TestClassTable rootClassTable;
    private TestClassTable subClassTable;
    private TestFuncTable rootFuncTable;
    private TestFuncTable subFuncTable;

    public AdditionalTestDocsSetter(TestClassTable rootClassTable, TestClassTable subClassTable,
            TestFuncTable rootFuncTable, TestFuncTable subFuncTable) {
        this.rootClassTable = rootClassTable;
        this.subClassTable = subClassTable;
        this.rootFuncTable = rootFuncTable;
        this.subFuncTable = subFuncTable;
    }

    public void set(AdditionalTestDocs testDocs) {
        // last set data in testDocs is used first

        for (int i = testDocs.getClassTestDocs().size() - 1; i >= 0; i--) {
            AdditionalClassTestDoc classTestDoc = testDocs.getClassTestDocs().get(i);
            setClass(classTestDoc.getQualifiedName(), classTestDoc.getTestDoc(),
                    classTestDoc instanceof AdditionalPage);
        }
        for (int i = testDocs.getFuncTestDocs().size() - 1; i >= 0; i--) {
            AdditionalFuncTestDoc testDoc = testDocs.getFuncTestDocs().get(i);
            setFunction(testDoc);
        }
    }

    public static String additionalTestDocKey(String qualifiedName) {
        // class qualified name must be unique
        return "_Additional_" + qualifiedName;
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
        newClass.setKey(additionalTestDocKey(qualifiedName));
        newClass.setQualifiedName(qualifiedName);
        newClass.setTestDoc(testDoc);
        subClassTable.addTestClass(newClass);
        return newClass;
    }

    // return the newly set TestFunction instance or already set instance.
    private TestFunction setFunction(AdditionalFuncTestDoc testDoc) {
        for (TestFunction testFunction : subFuncTable.getTestFunctions()) {
            // TODO method overload is not supported
            if (testDoc.getQualifiedName().equals(testFunction.getQualifiedName())) {
                return testFunction;
            }
        }

        for (TestFunction testFunction : rootFuncTable.getTestFunctions()) {
            // TODO method overload is not supported
            if (testDoc.getQualifiedName().equals(testFunction.getQualifiedName())) {
                return testFunction;
            }
        }

        TestFunction newFunction;
        if (testDoc instanceof AdditionalMethodTestDoc) {
            AdditionalMethodTestDoc methodTestDoc = (AdditionalMethodTestDoc) testDoc;
            TestClass testClass = setClass(methodTestDoc.getClassQualifiedName(), null, false);
            TestMethod newMethod = new TestMethod();
            newMethod.setTestClassKey(testClass.getKey());
            newMethod.setTestClass(testClass);
            testClass.addTestMethod(newMethod);
            newFunction = newMethod;
        } else {
            newFunction = new TestFunction();
        }
        newFunction.setKey(additionalTestDocKey(testDoc.getQualifiedName()));
        newFunction.setQualifiedName(testDoc.getQualifiedName());
        newFunction.setTestDoc(testDoc.getTestDoc());
        subFuncTable.addTestFunction(newFunction);
        return newFunction;
    }

}
