package org.sahagin.runlib.runresultsgen;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.Test;
import org.sahagin.TestBase;
import org.sahagin.share.CommonPath;
import org.sahagin.share.Config;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlUtils;

public class RunResultGenerateHookTest extends TestBase {

    private static File getTestCapturePath(File capturesDir, int counter) {
        return new File(capturesDir, counter + ".png");
    }

    private class MavenInvokeResult {
        private String invokerName;
        private final List<String> stdOuts = new ArrayList<String>(1024);
        private final List<String> stdErrs = new ArrayList<String>(1024);
        private boolean succeeded = false;

        private MavenInvokeResult(String invokerName) {
            this.invokerName = invokerName;
        }

        private void printStdOursAndErrs() {
            System.out.println("---- Maven Invoker [" + invokerName + "] std out ----");
            for (String stdOut : stdOuts) {
                System.out.println(stdOut);
            }
            System.err.println("---- Maven Invoker [" + invokerName + "] std error ----");
            for (String stdErr : stdErrs) {
                System.err.println(stdErr);
            }
            System.err.println("-------------------------------------------");
        }
    }

    // returns stdOut and stdErr pair
    // - output and error handler will be set to the request
    private MavenInvokeResult mavenInvoke(InvocationRequest request, String name) {
        final MavenInvokeResult result = new MavenInvokeResult(name);
        request.setOutputHandler(new InvocationOutputHandler() {

            @Override
            public void consumeLine(String arg) {
                result.stdOuts.add(arg);
            }
        });
        request.setErrorHandler(new InvocationOutputHandler() {

            @Override
            public void consumeLine(String arg) {
                result.stdErrs.add(arg);
            }
        });

        Invoker invoker = new DefaultInvoker();
        try {
            result.succeeded = (invoker.execute(request).getExitCode() == 0);
        } catch (MavenInvocationException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private void captureAssertion(String className, String methodName,
            File reportInputDir, int counterMax) {
        File capturesDir = new File(mkWorkDir(), "captures");
        File testMainCaptureDir
        = new File(CommonPath.inputCaptureRootDir(reportInputDir), className);
        for (int i = 1; i <= counterMax; i++) {
            assertFileByteContentsEquals(getTestCapturePath(capturesDir, i),
                    new File(testMainCaptureDir, String.format("%s/00%d.png", methodName, i)));
        }
        // TODO assert file for counterMax + 1 does not exist
    }

    private void testResultAssertion(String className, String methodName,
            File reportInputDir) throws YamlConvertException {
        File testMainResultDir
        = new File(CommonPath.runResultRootDir(reportInputDir), className);
        Map<String, Object> actualYamlObj = YamlUtils.load(new File(testMainResultDir, methodName));
        Map<String, Object> expectedYamlObj = YamlUtils.load(
                new File(new File(testResourceDir("expected"), className),  methodName));
        assertYamlEquals(expectedYamlObj, actualYamlObj);
    }

    // Execute this test by Maven, or set system property maven.home or set environment value M2_HOME
    @Test
    public void test() throws MavenInvocationException, YamlConvertException, IOException {
        // generate sahagin temp jar for test from the already generated class files
        InvocationRequest jarGenRequest = new DefaultInvocationRequest();
        jarGenRequest.setProfiles(Arrays.asList("sahagin-temp-jar-gen"));
        jarGenRequest.setGoals(Arrays.asList("jar:jar"));
        MavenInvokeResult jarGenResult = mavenInvoke(jarGenRequest, "jarGen");
        if (!jarGenResult.succeeded) {
            jarGenResult.printStdOursAndErrs();
            fail("fail to generate jar");
        }

        // set up test data on the working directory
        File workDir = mkWorkDir().getAbsoluteFile();
        Config conf = new Config(workDir);
        conf.setTestDir(new File(workDir, "src/test/java"));
        conf.setRunTestOnly(true);
        YamlUtils.dump(conf.toYamlObject(), new File(workDir, "sahagin.yml"));
        FileUtils.copyFile(new File("pom.xml"), new File(workDir, "pom.xml"));
        FileUtils.copyDirectory(testResourceDir("src"), new File(workDir, "src"));
        FileUtils.copyDirectory(testResourceDir("expected/captures"), new File(workDir, "captures"));

        // execute test on the working directory
        InvocationRequest testRequest = new DefaultInvocationRequest();
        testRequest.setGoals(Arrays.asList("clean", "test"));
        testRequest.setProfiles(Arrays.asList("sahagin-jar-test"));
        String jarPathOpt = "-Dsahagin.temp.jar="
                + new File("target/sahagin-temp.jar").getAbsolutePath();
        testRequest.setMavenOpts(jarPathOpt);
        testRequest.setBaseDirectory(workDir);
        MavenInvokeResult testResult = mavenInvoke(testRequest, "test");

        // check test output
        File reportInputDir = conf.getRootBaseReportInputDataDir();
        try {
            String normalTest = "normal.TestMain";
            captureAssertion(normalTest, "noTestDocMethodFailTest", reportInputDir, 1);
            captureAssertion(normalTest, "stepInCaptureTest", reportInputDir, 4);
            captureAssertion(normalTest, "successTest", reportInputDir, 2);
            captureAssertion(normalTest, "testDocMethodFailTest", reportInputDir, 1);
            testResultAssertion(normalTest, "noTestDocMethodFailTest", reportInputDir);
            testResultAssertion(normalTest, "stepInCaptureTest", reportInputDir);
            testResultAssertion(normalTest, "successTest", reportInputDir);
            testResultAssertion(normalTest, "testDocMethodFailTest", reportInputDir);

            String extendsTest = "extendstest.ExtendsTest";
            captureAssertion(extendsTest, "extendsTest", reportInputDir, 5);
            testResultAssertion(extendsTest, "extendsTest", reportInputDir);

            String implementsTest = "implementstest.ImplementsTest";
            captureAssertion(implementsTest, "implementsTest", reportInputDir, 3);
            testResultAssertion(implementsTest, "implementsTest", reportInputDir);
        } catch (AssertionError e) {
            testResult.printStdOursAndErrs();
            throw e;
        }
    }

}
