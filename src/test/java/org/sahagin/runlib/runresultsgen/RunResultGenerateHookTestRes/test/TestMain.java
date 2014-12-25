package org.sahagin.runlib.runresultsgen.RunResultGenerateHookTestRes.test;

import static org.junit.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.sahagin.TestBase;
import org.sahagin.runlib.external.TestDoc;
import org.sahagin.runlib.external.adapter.AdapterContainer;
import org.sahagin.runlib.external.adapter.ScreenCaptureAdapter;
import org.sahagin.runlib.runresultsgen.RunResultGenerateHookTest;

// this test is executed only called from RunResultGenerateHookTest
public class TestMain extends TestBase {

    public int counter = 1;

    private boolean calledFromMavenInvoker() {
        return System.getenv("MAVEN_INVOKER") != null;
    }

    @Before
    public void setUp() {
        // dummy screen capture handler for this test
        AdapterContainer.globalInstance().setScreenCaptureAdapter(new ScreenCaptureAdapter() {

            @Override
            public byte[] captueScreen() {
                File captureFile = RunResultGenerateHookTest.getTestCapturePath(counter);
                counter++;
                try {
                    return IOUtils.toByteArray(new FileInputStream(captureFile));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Test
    public void successTest() {
        assumeTrue(calledFromMavenInvoker());
        assertEquals(1, 1);
        assertThat(1 + 1, is(2));
    }

    @Test
    public void stepInCaptureTest() {
        assumeTrue(calledFromMavenInvoker());
        noStepInCaptureMethod();
        stepInCaptureMethod();
    }

    @TestDoc(value = "Doc: noStepInCaptureMethod")
    public void noStepInCaptureMethod() {
        assertEquals(2, 2);
        assertEquals(2, 2);
    }

    @TestDoc(value = "Doc: stepInCaptureMethod", stepInCapture = true)
    public void stepInCaptureMethod() {
        assertEquals(2, 2);
        assertEquals(2, 2);
    }

    @Test
    public void testDocMethodFailTest() {
        assumeTrue(calledFromMavenInvoker());
        testDocMethod();
    }

    @TestDoc("Doc: testDocMethod")
    public void testDocMethod() {
        assertEquals(1, 2);
    }

    @Test
    public void noTestDocMethodFailTest() {
        assumeTrue(calledFromMavenInvoker());
        noTestDocMethod();
    }

    public void noTestDocMethod() {
        assertEquals(1, 2);
    }

}
