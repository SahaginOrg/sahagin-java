package org.sahagin.runlib.runresultsgen;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.Test;
import org.sahagin.TestBase;
import org.sahagin.runlib.runresultsgen.RunResultGenerateHookTestRes.test.TestMain;
import org.sahagin.share.CommonPath;
import org.sahagin.share.Config;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlUtils;

public class RunResultGenerateHookTest extends TestBase {

    public static final File getTestCapturePath(int counter) {
        RunResultGenerateHookTest instance = new RunResultGenerateHookTest();
        return new File(instance.testResourceDir("test"), counter + ".png");
    }

    // TODO calling another maven process make it hard to analyze this test result..
    // Execute this test by Maven, or set system property maven.home or set environment value M2_HOME
    @Test
    public void test() throws MavenInvocationException, YamlConvertException {
        File workDir = mkWorkDir("test").getAbsoluteFile();
        File yamlFile = new File(workDir, "sahagin.yml");
        Config conf = new Config(workDir);
        conf.setTestDir(testJavaResourceDir("test").getAbsoluteFile());
        conf.setRunTestOnly(true);
        YamlUtils.dump(conf.toYamlObject(), yamlFile);

        InvocationRequest request = new DefaultInvocationRequest();
        request.setGoals(Arrays.asList("jar:jar", "test", "-q"));
        request.addShellEnvironment("MAVEN_INVOKER", "on");
        request.setOutputHandler(new InvocationOutputHandler() {

            @Override
            public void consumeLine(String arg0) {
                // ignore standard output
            }
        });
        request.setErrorHandler(new InvocationOutputHandler() {

            @Override
            public void consumeLine(String arg0) {
                // ignore standard error
            }
        });
        String jarnameOpt = "-Dsahagin.jarname=sahagin-temp-for-test";
        // TODO output directory name "target" is hard coded
        String javaagentOpt
        = "-Dsahagin.javaagent=-javaagent:target/sahagin-temp-for-test.jar=" + yamlFile.getPath();
        String testOpt = "-Dtest=" + TestMain.class.getCanonicalName();
        request.setMavenOpts(jarnameOpt + " " + javaagentOpt + " " + testOpt);

        Invoker invoker = new DefaultInvoker();
        invoker.execute(request);

        File reportInputDir = conf.getRootBaseReportInputDataDir();
        captureAssertion("noTestDocMethodFailTest", reportInputDir, 1);
        captureAssertion("stepInCaptureTest", reportInputDir, 4);
        captureAssertion("successTest", reportInputDir, 2);
        captureAssertion("testDocMethodFailTest", reportInputDir, 1);
        testResultAssertion("noTestDocMethodFailTest", reportInputDir);
        testResultAssertion("stepInCaptureTest", reportInputDir);
        testResultAssertion("successTest", reportInputDir);
        testResultAssertion("testDocMethodFailTest", reportInputDir);
    }

    private void captureAssertion(String methodName, File reportInputDir, int counterMax) {
        File testMainCaptureDir = new File(
                CommonPath.inputCaptureRootDir(reportInputDir), TestMain.class.getCanonicalName());
        for (int i = 1; i <= counterMax; i++) {
            assertFileByteContentsEquals(
                    getTestCapturePath(i),
                    new File(testMainCaptureDir, String.format("%s/00%d.png", methodName, i)));
        }
        // TODO assert file for counterMax + 1 does not exist
    }

    private void testResultAssertion(String methodName, File reportInputDir)
            throws YamlConvertException {
        File testMainResultDir = new File(
                CommonPath.runResultRootDir(reportInputDir), TestMain.class.getCanonicalName());
        Map<String, Object> actualYamlObj = YamlUtils.load(new File(testMainResultDir, methodName));
        Map<String, Object> expectedYamlObj = YamlUtils.load(new File(testResourceDir("test"), methodName));
        assertYamlEquals(expectedYamlObj, actualYamlObj);
    }

}
