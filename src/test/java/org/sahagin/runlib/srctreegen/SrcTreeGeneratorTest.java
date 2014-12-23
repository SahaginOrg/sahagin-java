package org.sahagin.runlib.srctreegen;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sahagin.TestBase;
import org.sahagin.runlib.external.adapter.junit4.JUnit4Adapter;
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
        SrcTreeGenerator gen = new SrcTreeGenerator(null);
        SrcTree srcTree;
        try {
            srcTree = gen.generateWithRuntimeClassPath(testDir, "UTF-8");
        } catch (IllegalTestScriptException e) {
            throw new RuntimeException(e);
        }
        File outputDir = mkWorkDir(methodName);
        File actualSrcTreeFile = new File(outputDir, "actualSrcTree");
        YamlUtils.dump(srcTree.toYamlObject(), actualSrcTreeFile);
        File expectedSrcTreeFile = new File(testResourceDir(methodName), "expectedSrcTree");
        // copy expected file next to actual file to make it easy for developers to check file difference
        File copiedExpectedSrcTreeFile = new File(outputDir, "copiedExpectedSrcTree");
        try {
            FileUtils.copyFile(expectedSrcTreeFile, copiedExpectedSrcTreeFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        TestBase.assertFileContentsEquals(expectedSrcTreeFile, copiedExpectedSrcTreeFile);
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
