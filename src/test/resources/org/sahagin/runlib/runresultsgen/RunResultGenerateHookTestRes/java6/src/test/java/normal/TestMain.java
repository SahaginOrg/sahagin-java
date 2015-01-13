package normal;

import static org.junit.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.sahagin.runlib.external.CaptureStyle;
import org.sahagin.runlib.external.TestDoc;

import base.Java6TestBase;

public class TestMain extends Java6TestBase {

    @Test
    public void successTest() {
        assertEquals(1, 1);
        assertThat(1 + 1, is(2));
    }

    @Test
    public void stepInCaptureTest() {
        noStepInCaptureMethod();
        stepInCaptureMethod();
    }

    @TestDoc(value = "Doc: noStepInCaptureMethod")
    public void noStepInCaptureMethod() {
        assertEquals(2, 2);
        assertEquals(2, 2);
    }

    @TestDoc(value = "Doc: stepInCaptureMethod", capture = CaptureStyle.STEP_IN)
    public void stepInCaptureMethod() {
        assertEquals(2, 2);
        assertEquals(2, 2);
    }

    @Test
    public void testDocMethodFailTest() {
        testDocMethod();
    }

    @TestDoc("Doc: testDocMethod")
    public void testDocMethod() {
        assertEquals(1, 2);
    }

    @Test
    public void noTestDocMethodFailTest() {
        noTestDocMethod();
    }

    public void noTestDocMethod() {
        assertEquals(1, 2);
    }
    
    @Test
    public void multiLineStatementTest() {
        assertThat(
                1, is(1));
    }

}
