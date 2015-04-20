package org.sahagin.report;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Assume;
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
            file = new File(testResourceDir(), "selenium/win/chromedriver.exe");
        } else {
            throw new RuntimeException("not supported OS environment");
        }
        return file.getAbsolutePath();
    }

    // returns generated index.html
    private File generateTestReport()
            throws IllegalDataStructureException, IllegalTestScriptException {
        SysMessages.globalInitialize(AcceptableLocales.getInstance(null));
        HtmlReport report = new HtmlReport();
        String subDirName = "reportGenerateShouldSucceedWithoutError/output";
        clearWorkDir(subDirName);
        File outputDir = mkWorkDir(subDirName);
        report.generate(
                testResourceDir("reportGenerateShouldSucceedWithoutError/input"),
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

    private void seleniumTestRun(File indexHtml, boolean chrome) {
        WebDriver driver = null;

        try {
            if (chrome) {
                // Don't execute ChromeDriver test on Travis CI
                // because the test will freeze..
                boolean onTravisCI =
                        StringUtils.equals(System.getenv("CI"), "true")
                        && StringUtils.equals(System.getenv("TRAVIS"), "true");
                Assume.assumeTrue(!onTravisCI);

                System.setProperty("webdriver.chrome.driver", chromeDriverPath());
                driver = new ChromeDriver();
            } else {
                driver = new FirefoxDriver();
            }
        } catch (Exception e) {
            quietQuit(driver);
            Assume.assumeNoException("This Driver can not work on this enviroment", e);
        }

        // TODO need more check such as Js error check
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
    public void reportGenerationShouldSucceed()
            throws IllegalDataStructureException, IllegalTestScriptException {
        generateTestReport();
    }

    @Test
    public void generatedReportShouldWorkOnFirefox()
            throws IllegalDataStructureException, IllegalTestScriptException {
        File indexHtml = generateTestReport();
        seleniumTestRun(indexHtml, false);
    }

    @Test
    public void generatedReportShouldWorkOnChrome()
            throws IllegalDataStructureException, IllegalTestScriptException {
        File indexHtml = generateTestReport();
        seleniumTestRun(indexHtml, true);
    }

    private void quietQuit(WebDriver driver) {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception ignored) {}
        }
    }

}
