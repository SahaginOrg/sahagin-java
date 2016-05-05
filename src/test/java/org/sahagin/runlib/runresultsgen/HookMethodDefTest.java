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
        private final List<String> stdOuts = new ArrayList<String>(1024);
        private final List<String> stdErrs = new ArrayList<String>(1024);
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
    public void java6() throws MavenInvocationException, YamlConvertException, IOException {
        String subDirName = "java6";
        generateTempJar(subDirName);
        Pair<MavenInvokeResult, JavaConfig> pair = invokeChildTest(subDirName, null);

        // check test output
        File reportIntermediateDir = pair.getRight().getRootBaseReportIntermediateDataDir();

        try {
            String normalTest = "normal.TestMain";
            testResultAssertion(normalTest, "noTestDocMethodFailTest", reportIntermediateDir, false);
            testResultAssertion(normalTest, "stepInCaptureTest", reportIntermediateDir, true);
            testResultAssertion(normalTest, "successTest", reportIntermediateDir, true);
            testResultAssertion(normalTest, "testDocMethodFailTest", reportIntermediateDir, false);
            testResultAssertion(normalTest, "innerClassTest", reportIntermediateDir, true);
            testResultAssertion(normalTest, "anonymousClassTest", reportIntermediateDir, true);
            testResultAssertion(normalTest, "multiLineStatementTest", reportIntermediateDir, true);
            testResultAssertion(normalTest, "localVarTest", reportIntermediateDir, true);
            testResultAssertion(normalTest, "testStepLabelTest", reportIntermediateDir, true);
            // Check only if test has been succeeded for the moment
            // since other result such as screen captures are still buggy..
            // TODO fix these bugs
            testResultAssertion(normalTest, "multiStatementInALineTest", reportIntermediateDir, true);
            captureAssertion(subDirName, normalTest, "noTestDocMethodFailTest", reportIntermediateDir, 1);
            captureAssertion(subDirName, normalTest, "stepInCaptureTest", reportIntermediateDir, 4);
            captureAssertion(subDirName, normalTest, "successTest", reportIntermediateDir, 2);
            captureAssertion(subDirName, normalTest, "testDocMethodFailTest", reportIntermediateDir, 1);
            captureAssertion(subDirName, normalTest, "innerClassTest", reportIntermediateDir, 1);
            captureAssertion(subDirName, normalTest, "anonymousClassTest", reportIntermediateDir, 1);
            captureAssertion(subDirName, normalTest, "multiLineStatementTest", reportIntermediateDir, 1);
            captureAssertion(subDirName, normalTest, "localVarTest", reportIntermediateDir, 1);
            captureAssertion(subDirName, normalTest, "testStepLabelTest", reportIntermediateDir, 4);

            String extendsTest = "extendstest.ExtendsTest";
            testResultAssertion(extendsTest, "extendsTest", reportIntermediateDir, true);
            captureAssertion(subDirName, extendsTest, "extendsTest", reportIntermediateDir, 5);

            String implementsTest = "implementstest.ImplementsTest";
            testResultAssertion(implementsTest, "implementsTest", reportIntermediateDir, true);
            captureAssertion(subDirName, implementsTest, "implementsTest", reportIntermediateDir, 3);

            String captureTest = "capturetest.TestMain";
            testResultAssertion(captureTest, "captureTest", reportIntermediateDir, true);
            captureAssertion(subDirName, captureTest, "captureTest", reportIntermediateDir, 5);

            String multiExtendsTest1 = "multiextendstest.Test1";
            testResultAssertion(multiExtendsTest1, "test1", reportIntermediateDir, true);
            captureAssertion(subDirName, multiExtendsTest1, "test1", reportIntermediateDir, 1);

            String multiExtendsTest2 = "multiextendstest.Test2";
            testResultAssertion(multiExtendsTest2, "test2", reportIntermediateDir, true);
            captureAssertion(subDirName, multiExtendsTest2, "test2", reportIntermediateDir, 1);
        } catch (AssertionError e) {
            pair.getLeft().printStdOutsAndErrs();
            throw e;
        }
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
        File reportIntermediateDir = pair.getRight().getRootBaseReportIntermediateDataDir();
        try {
            String java8featuresTest = "java8features.TestMain";
            captureAssertion(subDirName, java8featuresTest, "streamApiCallTest", reportIntermediateDir, 1);
            captureAssertion(subDirName, java8featuresTest, "defaultInterfaceTest", reportIntermediateDir, 1);
            testResultAssertion(java8featuresTest, "streamApiCallTest", reportIntermediateDir, true);
            testResultAssertion(java8featuresTest, "defaultInterfaceTest", reportIntermediateDir, true);
        } catch (AssertionError e) {
            pair.getLeft().printStdOutsAndErrs();
            throw e;
        }
    }

}
