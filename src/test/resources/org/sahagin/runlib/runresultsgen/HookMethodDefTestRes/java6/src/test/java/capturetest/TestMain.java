package capturetest;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.sahagin.runlib.external.CaptureStyle;
import org.sahagin.runlib.external.TestDoc;

import base.Java6TestBase;

public class TestMain extends Java6TestBase {

    // - check capture line number
    // - check if capture is taken just after the target line call
    @Test
    public void captureTest() {
        assertCaptureNextCounter(1);
        subMethod();
        assertCaptureNextCounter(5);
        assertCaptureNextCounterNoTestDoc(6);
        assertCaptureNextCounterNoTestDoc(6);
    }

    @TestDoc(value = "Doc: subMethod", capture = CaptureStyle.STEP_IN)
    public void subMethod() {
        assertCaptureNextCounter(2);
        assertCaptureNextCounter(3);
    }

    @TestDoc("Doc: assertCaptureNextCounter")
    public void assertCaptureNextCounter(int expected) {
        assertThat(nextCounter(), is(expected));
    }

    public void assertCaptureNextCounterNoTestDoc(int expected) {
        assertThat(nextCounter(), is(expected));
    }

}
