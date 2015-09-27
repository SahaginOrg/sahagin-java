package org.sahagin.runlib.srctreegen.SrcTreeGeneratorTestRes.multiLinesAndStatements.input;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.sahagin.runlib.external.CaptureStyle;
import org.sahagin.runlib.external.TestDoc;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


public class TestMain {

    @Test
    public void test() {
        testDocMethod(1, 2);testDocMethod(1, 3);testDocMethod(1,
                4);
        this
        .testDocMethod(
                1,
                5
                );
    }

    @TestDoc("Doc:testDocMethod")
    private void testDocMethod(int arg1, int arg2) {}

}
