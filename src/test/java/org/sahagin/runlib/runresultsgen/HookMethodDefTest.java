package org.sahagin.runlib.runresultsgen;

import static org.junit.Assert.fail;
import static org.junit.Assume.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.Test;
import org.sahagin.TestBase;
import org.sahagin.share.CommonPath;
import org.sahagin.share.JavaConfig;
import org.sahagin.share.runresults.LineScreenCapture;
import org.sahagin.share.runresults.RootMethodRunResult;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlUtils;

// This test must be executed by Maven,
// or executed with Maven home setting
// (system property maven.home or set environment value M2_HOME) and JAVA_HOME environment value
public class HookMethodDefTest extends TestBase {

    private static File getTestCapturePath(File capturesDir, int counter) {
        return new File(capturesDir, counter + ".png");
    }

    private class MavenInvokeResult {
        private String invokerName;
        private final List<String> stdOuts = new ArrayList<>(1024);
        private final List<String> stdErrs = new ArrayList<>(1024);
        private boolean succeeded = false;

        private MavenInvokeResult(String invokerName) {
            this.invokerName = invokerName;
        }

        private void printStdOutsAndErrs() {
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

    private void captureAssertion(String subDirName, String className, String methodName,
            File reportIntermediateDir, int counterMax) {
        File capturesDir = new File(mkWorkDir(subDirName), "captures");
        File testMainCaptureDir
        = new File(CommonPath.inputCaptureRootDir(reportIntermediateDir), className);
        for (int i = 1; i <= counterMax; i++) {
            assertFileByteContentsEquals(getTestCapturePath(capturesDir, i),
                    new File(testMainCaptureDir, String.format("%s/00%d.png", methodName, i)));
        }
        // TODO assert file for counterMax + 1 does not exist
    }

    // returns pair of whole method execution time and
    // each screen capture line execution time list in the executed order
    private Pair<Integer, List<Integer>> getTestExecutionTimes(
            String className, String methodName, File reportIntermediateDir) {
        File testMainResultDir
        = new File(CommonPath.runResultRootDir(reportIntermediateDir), className);
        File actualFile = new File(testMainResultDir, methodName);
        if (!actualFile.exists()) {
            fail(actualFile + " does not exist");
        }
        Map<String, Object> actualYamlObj = YamlUtils.load(actualFile);
        RootMethodRunResult result = new RootMethodRunResult();
        try {
            result.fromYamlObject(actualYamlObj);
        } catch (YamlConvertException e) {
            throw new RuntimeException(e);
        }
        List<Integer> lineScreenCatureExecutionTimes
        = new ArrayList<>(result.getLineScreenCaptures().size());
        for (LineScreenCapture lineScreenCapture : result.getLineScreenCaptures()) {
            lineScreenCatureExecutionTimes.add(lineScreenCapture.getExecutionTime());
        }
        return Pair.of(result.getExecutionTime(), lineScreenCatureExecutionTimes);
    }

    private void testResultAssertion(String className, String methodName,
            File reportIntermediateDir, boolean checksTestSuccess) throws YamlConvertException {
        File testMainResultDir
        = new File(CommonPath.runResultRootDir(reportIntermediateDir), className);
        File actualFile = new File(testMainResultDir, methodName);
        if (!actualFile.exists()) {
            fail(actualFile + " does not exist");
        }
        Map<String, Object> actualYamlObj = YamlUtils.load(actualFile);
        if (checksTestSuccess) {
            // check runFailures entry does not exist
            // since assertYamlEquals method does not check this
            assertThat(actualYamlObj.containsKey("runFailures"), is(not(true)));
        }
        File expectedFile = new File(new File(testResourceDir("expected"), className),  methodName);
        if (!expectedFile.exists()) {
            fail(expectedFile + " does not exist");
        }
        Map<String, Object> expectedYamlObj = YamlUtils.load(expectedFile);
        assertYamlEquals(expectedYamlObj, actualYamlObj, true);
    }

    private void generateTempJar(String subDirName) {
        // generate sahagin temp jar for test from the already generated class files
        InvocationRequest jarGenRequest = new DefaultInvocationRequest();
        if (System.getProperty("sahagin.maven.java.home") != null) {
            jarGenRequest.setJavaHome(new File(System.getProperty("sahagin.maven.java.home")));
        }
        jarGenRequest.setProfiles(Arrays.asList("sahagin-temp-jar-gen"));
        jarGenRequest.setGoals(Arrays.asList("jar:jar"));
        MavenInvokeResult jarGenResult = mavenInvoke(jarGenRequest, subDirName + ":jarGen");
        if (!jarGenResult.succeeded) {
            jarGenResult.printStdOutsAndErrs();
            fail("fail to generate jar");
        }
    }

    private String mavenJreVersion() {
        InvocationRequest versionRequest = new DefaultInvocationRequest();
        if (System.getProperty("sahagin.maven.java.home") != null) {
            versionRequest.setJavaHome(new File(System.getProperty("sahagin.maven.java.home")));
        }
        versionRequest.setGoals(Arrays.asList("-v"));
        MavenInvokeResult versionResult = mavenInvoke(versionRequest, "version");
        for (String stdOut : versionResult.stdOuts) {
            String[] entries = stdOut.split(",");
            for (String entry : entries) {
                String[] keyValue = entry.split(":");
                if (keyValue.length != 2) {
                    continue;
                }
                if (keyValue[0].trim().equals("Java version")) {
                    return keyValue[1].trim();
                }
            }
        }
        versionResult.printStdOutsAndErrs();
        throw new RuntimeException(String.format("fails to get JRE verion"));
    }

    private Pair<MavenInvokeResult, JavaConfig> invokeChildTest(
            String subDirName, String additionalProfile) throws IOException {
        // set up working directory
        clearWorkDir(subDirName);
        File workDir = mkWorkDir(subDirName).getAbsoluteFile();
        JavaConfig conf = new JavaConfig(workDir);
        conf.setTestDir(new File(workDir, "src/test/java"));
        conf.setRunTestOnly(true);
        YamlUtils.dump(conf.toYamlObject(), new File(workDir, "sahagin.yml"));
        FileUtils.copyFile(new File("pom.xml"), new File(workDir, "pom.xml"));
        FileUtils.copyDirectory(testResourceDir(subDirName + "/src"), new File(workDir, "src"));
        FileUtils.copyDirectory(testResourceDir("expected/captures"), new File(workDir, "captures"));

        // execute test
        InvocationRequest testRequest = new DefaultInvocationRequest();
        if (System.getProperty("sahagin.maven.java.home") != null) {
            testRequest.setJavaHome(new File(System.getProperty("sahagin.maven.java.home")));
        }
        testRequest.setGoals(Arrays.asList("clean", "test"));
        if (additionalProfile == null) {
            testRequest.setProfiles(Arrays.asList("sahagin-jar-test"));
        } else {
            testRequest.setProfiles(Arrays.asList("sahagin-jar-test", additionalProfile));
        }
        String jarPathOpt = "-Dsahagin.temp.jar="
                + new File("target/sahagin-temp.jar").getAbsolutePath();
        testRequest.setMavenOpts(jarPathOpt);
        testRequest.setBaseDirectory(workDir);
        MavenInvokeResult testResult = mavenInvoke(testRequest, subDirName + ":test");

        return Pair.of(testResult, conf);
    }

    @Test
    public void java7() throws MavenInvocationException, YamlConvertException, IOException {
        String subDirName = "java7";
        generateTempJar(subDirName);
        Pair<MavenInvokeResult, JavaConfig> pair = invokeChildTest(subDirName, null);

        // check test output
        File intermediateDir = pair.getRight().getRootBaseRunOutputIntermediateDataDir();

        try {
            String normalTest = "normal.TestMain";
            testResultAssertion(normalTest, "noTestDocMethodFailTest", intermediateDir, false);
            testResultAssertion(normalTest, "stepInCaptureTest", intermediateDir, true);
            testResultAssertion(normalTest, "successTest", intermediateDir, true);
            testResultAssertion(normalTest, "testDocMethodFailTest", intermediateDir, false);
            testResultAssertion(normalTest, "innerClassTest", intermediateDir, true);
            testResultAssertion(normalTest, "anonymousClassTest", intermediateDir, true);
            testResultAssertion(normalTest, "multiLineStatementTest", intermediateDir, true);
            testResultAssertion(normalTest, "localVarTest", intermediateDir, true);
            testResultAssertion(normalTest, "testStepLabelTest", intermediateDir, true);
            // Check only if test has been succeeded for the moment
            // since other result such as screen captures are still buggy..
            // TODO fix these bugs
            testResultAssertion(normalTest, "multiStatementInALineTest", intermediateDir, true);
            captureAssertion(subDirName, normalTest, "noTestDocMethodFailTest", intermediateDir, 1);
            captureAssertion(subDirName, normalTest, "stepInCaptureTest", intermediateDir, 4);
            captureAssertion(subDirName, normalTest, "successTest", intermediateDir, 2);
            captureAssertion(subDirName, normalTest, "testDocMethodFailTest", intermediateDir, 1);
            captureAssertion(subDirName, normalTest, "innerClassTest", intermediateDir, 1);
            captureAssertion(subDirName, normalTest, "anonymousClassTest", intermediateDir, 1);
            captureAssertion(subDirName, normalTest, "multiLineStatementTest", intermediateDir, 1);
            captureAssertion(subDirName, normalTest, "localVarTest", intermediateDir, 1);
            captureAssertion(subDirName, normalTest, "testStepLabelTest", intermediateDir, 4);

            String extendsTest = "extendstest.ExtendsTest";
            testResultAssertion(extendsTest, "extendsTest", intermediateDir, true);
            captureAssertion(subDirName, extendsTest, "extendsTest", intermediateDir, 5);

            String implementsTest = "implementstest.ImplementsTest";
            testResultAssertion(implementsTest, "implementsTest", intermediateDir, true);
            captureAssertion(subDirName, implementsTest, "implementsTest", intermediateDir, 3);

            String captureTest = "capturetest.TestMain";
            testResultAssertion(captureTest, "captureTest", intermediateDir, true);
            captureAssertion(subDirName, captureTest, "captureTest", intermediateDir, 5);

            String multiExtendsTest1 = "multiextendstest.Test1";
            testResultAssertion(multiExtendsTest1, "test1", intermediateDir, true);
            captureAssertion(subDirName, multiExtendsTest1, "test1", intermediateDir, 1);

            String multiExtendsTest2 = "multiextendstest.Test2";
            testResultAssertion(multiExtendsTest2, "test2", intermediateDir, true);
            captureAssertion(subDirName, multiExtendsTest2, "test2", intermediateDir, 1);

            String executionTimeTest = "executiontimetest.TestMain";
            Pair<Integer, List<Integer>> execTimePair
            = getTestExecutionTimes(executionTimeTest, "executionTimeTest", intermediateDir);
            executionTimeTestAssertion(execTimePair.getLeft(), execTimePair.getRight());
            Pair<Integer, List<Integer>> testStepLabelExecTimePair
            = getTestExecutionTimes(executionTimeTest, "testStepLabelExecutionTimeTest", intermediateDir);
            testStepLabelExecutionTimeTestAssertion(
                    testStepLabelExecTimePair.getLeft(), testStepLabelExecTimePair.getRight());
        } catch (AssertionError e) {
            pair.getLeft().printStdOutsAndErrs();
            throw e;
        }
    }

    private void executionTimeTestAssertion(int totalExecTime, List<Integer> lineExecTimes) {
        int unitTime = 10;

        // screenCaptureLine execution times size
        assertThat(lineExecTimes.size(), is(12));

        // subMethod
        assertTrue(lineExecTimes.get(0) >= unitTime);
        assertTrue(lineExecTimes.get(1) >= unitTime);
        assertTrue(lineExecTimes.get(2) >= lineExecTimes.get(0) + lineExecTimes.get(1));

        // noStepInSubMethod
        assertTrue(lineExecTimes.get(3) >= unitTime);

        // recurseSubMethod
        assertTrue(lineExecTimes.get(4) >= unitTime);
        assertTrue(lineExecTimes.get(5) >= unitTime);
        assertTrue(lineExecTimes.get(6) >= unitTime);
        assertTrue(lineExecTimes.get(7) >= lineExecTimes.get(6));
        assertTrue(lineExecTimes.get(8) >= lineExecTimes.get(5) + lineExecTimes.get(7));
        assertTrue(lineExecTimes.get(9) >= lineExecTimes.get(4) + lineExecTimes.get(8));

        // returnSubMethod
        assertTrue(lineExecTimes.get(10) >= unitTime);
        assertTrue(lineExecTimes.get(11) >= lineExecTimes.get(10));

        // root method whole execution time
        assertTrue(totalExecTime
                >= lineExecTimes.get(2) + lineExecTimes.get(3)
                + lineExecTimes.get(9) + lineExecTimes.get(11));
    }

    private void testStepLabelExecutionTimeTestAssertion(
            int totalExecTime, List<Integer> lineExecTimes) {
        int unitTime = 10;

        // screenCaptureLine execution times size
        assertThat(lineExecTimes.size(), is(5));

        // step 1
        assertTrue(lineExecTimes.get(0) >= unitTime);
        assertTrue(lineExecTimes.get(1) >= unitTime);
        assertTrue(lineExecTimes.get(2) >= lineExecTimes.get(0) + lineExecTimes.get(1));

        // step 2
        assertTrue(lineExecTimes.get(3) >= unitTime);
        assertTrue(lineExecTimes.get(4) >= lineExecTimes.get(3));
    }

    @Test
    public void java8() throws IOException, YamlConvertException {
        // execute test only when Maven JRE version is equal or greater than 1.8
        BigDecimal thisVersion = new BigDecimal(mavenJreVersion().substring(0, 3));
        BigDecimal versionJava8 = new BigDecimal("1.8");
        assumeTrue(thisVersion.compareTo(versionJava8) >= 0);

        String subDirName = "java8";
        generateTempJar(subDirName);
        Pair<MavenInvokeResult, JavaConfig> pair = invokeChildTest(subDirName, "java8-compile");

        // check test output
        File intermediateDir = pair.getRight().getRootBaseRunOutputIntermediateDataDir();
        try {
            String java8featuresTest = "java8features.TestMain";
            captureAssertion(subDirName, java8featuresTest, "streamApiCallTest", intermediateDir, 1);
            captureAssertion(subDirName, java8featuresTest, "defaultInterfaceTest", intermediateDir, 1);
            testResultAssertion(java8featuresTest, "streamApiCallTest", intermediateDir, true);
            testResultAssertion(java8featuresTest, "defaultInterfaceTest", intermediateDir, true);
        } catch (AssertionError e) {
            pair.getLeft().printStdOutsAndErrs();
            throw e;
        }
    }
}
