package org.sahagin.report;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.sahagin.TestBase;
import org.sahagin.share.AcceptableLocales;
import org.sahagin.share.IllegalDataStructureException;
import org.sahagin.share.IllegalTestScriptException;
import org.sahagin.share.SysMessages;

public class HtmlReportTest extends TestBase {

    private String chromeDriverPath() {
        File file;
        if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
            file = new File(testResourceRoot(), "selenium/mac/chromedriver");
        } else if (SystemUtils.IS_OS_LINUX) {
            file = new File(testResourceRoot(), "selenium/linux/chromedriver");
        } else if (SystemUtils.IS_OS_WINDOWS) {
            file = new File(testResourceRoot(), "selenium/win/chromedriver.exe");
        } else {
            throw new RuntimeException("not supported OS environment");
        }
        return file.getAbsolutePath();
    }

    private String geckoDriverPath() {
        File file;
        if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
            file = new File(testResourceRoot(), "selenium/mac/geckodriver");
        } else if (SystemUtils.IS_OS_LINUX) {
            file = new File(testResourceRoot(), "selenium/linux/geckodriver");
        } else if (SystemUtils.IS_OS_WINDOWS) {
            file = new File(testResourceRoot(), "selenium/win/geckodriver.exe");
        } else {
            throw new RuntimeException("not supported OS environment");
        }
        return file.getAbsolutePath();
    }

    @BeforeClass
    public static void setUpClass() {
        SysMessages.globalInitialize(AcceptableLocales.getInstance(null));
    }

    // returns generated index.html
    private File generateNormalReport(String testName)
            throws IllegalDataStructureException, IllegalTestScriptException {
        HtmlReport report = new HtmlReport();
        String subDirName = testName + "/output";
        clearWorkDir(subDirName);
        File outputDir = mkWorkDir(subDirName);
        report.generate(
                Arrays.asList(testResourceDir(testName + "/input")),
                outputDir);
        assertThat(new File(outputDir, "captures").exists(), is(true));
        assertThat(new File(outputDir, "css").exists(), is(true));
        assertThat(new File(outputDir, "images").exists(), is(true));
        assertThat(new File(outputDir, "js").exists(), is(true));
        File indexHtml = new File(outputDir, "index.html");
        File shouldFailHtml = new File(outputDir, "reports/sample.SampleTest/shouldFail.html");
        File shouldSucceedHtml = new File(outputDir, "reports/sample.SampleTest/shouldSucceed.html");
        assertThat(indexHtml.exists(), is(true));
        assertThat(shouldFailHtml.exists(), is(true));
        assertThat(shouldSucceedHtml.exists(), is(true));
        return indexHtml;
    }

    // returns generated index.html
    // TODO check that capture files have been copied
    private File generateMultiReport(String testName)
            throws IllegalDataStructureException, IllegalTestScriptException {
        HtmlReport report = new HtmlReport();
        String subDirName = testName + "/output";
        clearWorkDir(subDirName);
        File outputDir = mkWorkDir(subDirName);
        report.generate(Arrays.asList(
                testResourceDir(testName + "/child1"),
                testResourceDir(testName + "/child2")),
                outputDir);
        File indexHtml = new File(outputDir, "index.html");
        File test1Html = new File(outputDir, "reports/test.Test1/test1.html");
        File test2Html = new File(outputDir, "reports/test.Test2/test2.html");
        assertThat(indexHtml.exists(), is(true));
        assertThat(test1Html.exists(), is(true));
        assertThat(test2Html.exists(), is(true));
        return indexHtml;
    }

    private void seleniumTestRun(File indexHtml, boolean chrome) {
        WebDriver driver;
        boolean onTravisCI =
                StringUtils.equals(System.getenv("CI"), "true")
                && StringUtils.equals(System.getenv("TRAVIS"), "true");
        boolean onCircleCI =
                StringUtils.equals(System.getenv("CI"), "true")
                && StringUtils.equals(System.getenv("CIRCLECI"), "true");
        if (chrome) {
            // Don't execute ChromeDriver test on Travis CI
            // because the test will freeze..
            Assume.assumeTrue(!onTravisCI);

            // CircleCI environment has its own ChromeDriver
            if (!onCircleCI) {
                System.setProperty("webdriver.chrome.driver", chromeDriverPath());
            }
            driver = new ChromeDriver();
        } else {
            if (!onCircleCI) {
                System.setProperty("webdriver.gecko.driver", geckoDriverPath());
            }
            driver = new FirefoxDriver();
        }

        // TODO need more check such as Js error check
        // TODO SHA1 encoded name test
        try {
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            String indexHtmlUrl = "file:///" + indexHtml.getAbsolutePath();
            driver.get(indexHtmlUrl);
            driver.findElement(By.linkText("sample.SampleTest.shouldSucceed")).click();
            driver.navigate().back();
            driver.findElement(By.linkText("sample.SampleTest.shouldFail")).click();
        } finally {
            quietQuit(driver);
        }
    }

    @Test
    public void generatedReportShouldWorkOnFirefox()
            throws IllegalDataStructureException, IllegalTestScriptException {
        File indexHtml = generateNormalReport("generatedReportShouldWork");
        seleniumTestRun(indexHtml, false);
    }

    @Test
    public void generatedReportShouldWorkOnChrome()
            throws IllegalDataStructureException, IllegalTestScriptException {
        File indexHtml = generateNormalReport("generatedReportShouldWork");
        seleniumTestRun(indexHtml, true);
    }

    @Test
    public void multiReportInputIntermediateDirShouldWork()
            throws IllegalDataStructureException, IllegalTestScriptException {
        generateMultiReport("multiReportInputIntermediateDirShouldWork");
    }

    private void quietQuit(WebDriver driver) {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception ignored) {}
        }
    }
}
