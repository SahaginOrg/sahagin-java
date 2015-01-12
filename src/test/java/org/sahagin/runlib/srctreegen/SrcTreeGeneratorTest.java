package org.sahagin.runlib.srctreegen;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sahagin.TestBase;
import org.sahagin.runlib.external.Locale;
import org.sahagin.runlib.external.adapter.AdapterContainer;
import org.sahagin.runlib.external.adapter.junit4.JUnit4Adapter;
import org.sahagin.share.AcceptableLocales;
import org.sahagin.share.IllegalTestScriptException;
import org.sahagin.share.srctree.SrcTree;
import org.sahagin.share.yaml.YamlUtils;

public class SrcTreeGeneratorTest extends TestBase {

    @BeforeClass
    public static void setUpClass() throws IOException {
        AcceptableLocales locales = AcceptableLocales.getInstance(null);
        AdapterContainer.globalInitialize(locales);
        // set RootFunctionAdapter
        new JUnit4Adapter().initialSetAdapter();
    }

    private void testMain(String subDirName, Locale userLocale) {
        File testSrcDir = new File(testResourceDir(subDirName), "input");
        AcceptableLocales locales = AcceptableLocales.getInstance(userLocale);
        SrcTreeGenerator gen = new SrcTreeGenerator(null, locales);
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
            assertYamlEquals(expectedYamlObj, actualYamlObj);
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
    // - when arguments of SubFunctionInvoke is another SubFunctionInvoke
    // - testDoc placeholder (variable name and index and this)
    // - @Test, @TestDoc, @Page annotation
    // - capture value of @TestDoc
    // - variable assignment with SubFunctionInvoke
    // - predefined methods ( assert, webDriver, findElement, by, click, sendKeys)
    // TODO this test checks multiple things,
    // so split to multiple test method when srcTree YAML format is fixed.
    @Test
    public void variousData() {
        testMain("variousData", null);
    }

    @Test
    public void utf8Character() {
        testMain("utf8Character", null);
    }

    @Test
    public void defaultLocale() {
        testMain("defaultLocale", null);
    }

    @Test
    public void jaJpLocale() {
        testMain("jaJpLocale", Locale.JA_JP);
    }

    @Test
    public void extendsTest() {
        testMain("extendsTest", null);
    }

    @Test
    public void implementsTest() {
        testMain("implementsTest", null);
    }

    @Test
    public void java8() {
        testMain("java8", null);
    }

}
