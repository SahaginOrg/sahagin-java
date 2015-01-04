package org.sahagin.runlib.srctreegen;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sahagin.TestBase;
import org.sahagin.runlib.external.adapter.junit4.JUnit4Adapter;
import org.sahagin.share.AcceptableLocales;
import org.sahagin.share.IllegalTestScriptException;
import org.sahagin.share.srctree.SrcTree;
import org.sahagin.share.yaml.YamlUtils;

public class SrcTreeGeneratorTest extends TestBase {

    @BeforeClass
    public static void setUpClass() throws IOException {
        // set RootFunctionAdapter
        new JUnit4Adapter().initialSetAdapter();
    }

    private void testMain(String methodName) {
        File testDir = testJavaResourceDir(methodName);
        AcceptableLocales locales = AcceptableLocales.getInstance(null);
        SrcTreeGenerator gen = new SrcTreeGenerator(null, locales);
        SrcTree srcTree;
        try {
            srcTree = gen.generateWithRuntimeClassPath(testDir, "UTF-8");
        } catch (IllegalTestScriptException e) {
            throw new RuntimeException(e);
        }
        // To get the same order srcTree YAML regardless of the JDT or JDK implementation,
        // sort before generating YAML file.
        srcTree.sort();
        Map<String, Object> actualYamlObj = srcTree.toYamlObject();
        File expectedSrcTreeFile = new File(testResourceDir(methodName), "srcTree");
        Map<String, Object> expectedYamlObj = YamlUtils.load(expectedSrcTreeFile);
        assertYamlEquals(expectedYamlObj, actualYamlObj);
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
        testMain("variousData");
    }

    @Test
    public void utf8Character() {
        testMain("utf8Character");
    }

}
