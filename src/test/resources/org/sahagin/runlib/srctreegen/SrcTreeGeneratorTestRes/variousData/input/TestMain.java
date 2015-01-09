package org.sahagin.runlib.srctreegen.SrcTreeGeneratorTestRes.variousData.input;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.sahagin.runlib.external.CaptureStyle;
import org.sahagin.runlib.external.TestDoc;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


public class TestMain {
    WebDriver wd;

    @SuppressWarnings("static-access")
    @Test
    public void testMethod1() {
        TestSub.subMethod();
        testDocMethod();
        noTestDocMethod();

        TestPage page = new TestPage();
        page.inenrClassCall();
        TestPage.staticMethod();
        page.staticMethod();
        page.argMethod("AAA", 999);
        page.argMethod(null, 0);
        page.recurseMethodCall();
        page.nest1("DDD");
        assertThat(page.getData(), is("EEE"));
        assertEquals(page.getData(), "EEE");
        if (wd == null) {
            return;
        }
        wd.get("***.com");
        wd.findElement(By.id("FFF")).click();
        wd.findElement(By.cssSelector("GGG")).sendKeys("HHH");
    }

    @Test
    @TestDoc("Doc:TestMethod2")
    public void testMethod2() {}

    @TestDoc(value = "Doc:testDocMethod", capture = CaptureStyle.STEP_IN)
    public void testDocMethod() {}

    public void noTestDocMethod() {}

}
