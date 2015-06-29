package org.sahagin.runlib.srctreegen;

import java.io.File;
import java.util.Map;

import org.junit.Test;
import org.sahagin.TestBase;
import org.sahagin.runlib.additionaltestdoc.AdditionalTestDocs;
import org.sahagin.runlib.external.Locale;
import org.sahagin.runlib.external.adapter.Adapter;
import org.sahagin.runlib.external.adapter.AdapterContainer;
import org.sahagin.runlib.external.adapter.JavaAdapterContainer;
import org.sahagin.runlib.external.adapter.javasystem.JavaSystemAdapter;
import org.sahagin.runlib.external.adapter.junit3.JUnit3Adapter;
import org.sahagin.runlib.external.adapter.junit4.JUnit4Adapter;
import org.sahagin.runlib.external.adapter.testng.TestNGAdapter;
import org.sahagin.share.AcceptableLocales;
import org.sahagin.share.IllegalTestScriptException;
import org.sahagin.share.srctree.SrcTree;
import org.sahagin.share.yaml.YamlUtils;

public class SrcTreeGeneratorTest extends TestBase {
    private static final JUnit3Adapter JUNIT3 = new JUnit3Adapter();
    private static final JUnit4Adapter JUNIT4 = new JUnit4Adapter();
    private static final TestNGAdapter TESTNG = new TestNGAdapter();

    private void testMain(String subDirName,
            AdditionalTestDocs additionalTestDocs, Locale userLocale, Adapter adapter) {
        // set JavaRootMethodAdapter
        JavaAdapterContainer.globalInitialize(
                AcceptableLocales.getInstance(null), adapter.getName());
        adapter.initialSetAdapter();
        File testSrcDir = new File(testResourceDir(subDirName), "input");
        AcceptableLocales locales = AcceptableLocales.getInstance(userLocale);
        SrcTreeGenerator gen = new SrcTreeGenerator(additionalTestDocs, locales);
        SrcTree srcTree;
        try {
            srcTree = gen.generateWithRuntimeClassPath(testSrcDir, "UTF-8");
        } catch (IllegalTestScriptException e) {
            throw new RuntimeException(e);
        }
        // To get the same order srcTree YAML regardless of the JDT or JDK implementation,
        // sort before generating YAML file.
        srcTree.sort();
        Map<String, Object> actualYamlObj = srcTree.toYamlObject();
        File expectedSrcTreeFile = new File(testResourceDir(subDirName), "srcTree");
        if (!expectedSrcTreeFile.exists()) {
            // output actual srcTree to use as expected srcTree
            YamlUtils.dump(actualYamlObj, new File(mkWorkDir(subDirName), "actualSrcTree"));
            throw new RuntimeException(expectedSrcTreeFile + " does not exist");
        }
        Map<String, Object> expectedYamlObj = YamlUtils.load(expectedSrcTreeFile);
        try {
            assertYamlEquals(expectedYamlObj, actualYamlObj, true);
        } catch (AssertionError e) {
            // output actual srcTree for debugging later
            YamlUtils.dump(actualYamlObj, new File(mkWorkDir(subDirName), "actualSrcTree"));
            throw e;
        }
    }

    // this test checks:
    // - calling method defined at the same class and the other class
    // - constructor with @TestDoc
    // - inner class
    // - static method
    // - recursive method call
    // - mutual recursive method call
    // - nested sub method call and it's method argument
    // - when arguments of SubMethodInvoke is another SubMethodInvoke
    // - testDoc placeholder (variable name and index and this)
    // - @Test, @TestDoc, @Page annotation
    // - capture value of @TestDoc
    // - variable assignment with SubMethodInvoke
    // - predefined methods ( assert, webDriver, findElement, by, click, sendKeys)
    // - multiple lines statement
    // - multiple statements in a line
    // TODO this test checks multiple things,
    // so split to multiple test method when srcTree YAML format is fixed.
    @Test
    public void variousData() {
        testMain("variousData", null, null, JUNIT4);
    }

    @Test
    public void classAndMethodKey() {
        testMain("classAndMethodKey", null, null, JUNIT4);
    }

    @Test
    public void utf8Character() {
        testMain("utf8Character", null, null, JUNIT4);
    }

    @Test
    public void defaultLocale() {
        testMain("defaultLocale", null, null, JUNIT4);
    }

    @Test
    public void jaJpLocale() {
        testMain("jaJpLocale", null, Locale.JA_JP, JUNIT4);
    }

    @Test
    public void extendsTest() {
        testMain("extendsTest", null, null, JUNIT4);
    }

    @Test
    public void implementsTest() {
        testMain("implementsTest", null, null, JUNIT4);
    }

    @Test
    public void exceptionHandler() {
        testMain("exceptionHandler", null, null, JUNIT4);
    }

    @Test
    public void varLengthArray() {
        testMain("varLengthArray", null, null, JUNIT4);
    }

    @Test
    public void childInvoke() {
        testMain("childInvoke", null, null, JUNIT4);
    }

    @Test
    public void localVar() {
        testMain("localVar", null, null, JUNIT4);
    }

    @Test
    public void field() {
        testMain("field", null, null, JUNIT4);
    }

    @Test
    public void testStepLabel() {
        testMain("testStepLabel", null, null, JUNIT4);
    }

    @Test
    public void assertTest() {
        AcceptableLocales locales = AcceptableLocales.getInstance(Locale.EN_US);
        JavaAdapterContainer.globalInitialize(locales, new JUnit4Adapter().getName());
        new JavaSystemAdapter().initialSetAdapter();
        AdditionalTestDocs testDocs
        = AdapterContainer.globalInstance().getAdditionalTestDocs();
        testMain("assertTest", testDocs, Locale.EN_US, JUNIT4);
    }

    @Test
    public void additionalTestDocs() {
        AcceptableLocales locales = AcceptableLocales.getInstance(Locale.EN_US);
        JavaAdapterContainer.globalInitialize(locales, new JUnit4Adapter().getName());
        new JUnit4Adapter().initialSetAdapter();
        AdditionalTestDocs testDocs
        = AdapterContainer.globalInstance().getAdditionalTestDocs();
        testMain("additionalTestDocs", testDocs, Locale.EN_US, JUNIT4);
    }

    @Test
    public void java8() {
        testMain("java8", null, null, JUNIT4);
    }

    @Test
    public void jUnit3() {
        testMain("jUnit3", null, null, JUNIT3);
    }

    @Test
    public void testNG() {
        testMain("testNG", null, null, TESTNG);
    }

}
