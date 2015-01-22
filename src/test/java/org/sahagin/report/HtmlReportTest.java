package org.sahagin.report;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.File;
import org.junit.Test;
import org.sahagin.TestBase;
import org.sahagin.share.IllegalDataStructureException;
import org.sahagin.share.IllegalTestScriptException;


public class HtmlReportTest extends TestBase {

    @Test
    public void reportGenerateShouldSucceedWithoutError()
            throws IllegalDataStructureException, IllegalTestScriptException {
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
        assertThat(new File(outputDir,
                "reports/sample.SampleTest/shouldFail.html").exists(), is(true));
        assertThat(new File(outputDir,
                "reports/sample.SampleTest/shouldSucceed.html").exists(), is(true));
        assertThat(new File(outputDir, "index.html").exists(), is(true));
    }

}
