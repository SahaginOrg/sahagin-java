import static org.junit.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.sahagin.runlib.external.CaptureStyle;
import org.sahagin.runlib.external.TestDoc;
import org.sahagin.runlib.external.adapter.AdapterContainer;
import org.sahagin.runlib.external.adapter.ScreenCaptureAdapter;

public class TestMain {

    private int counter = 1;
    
    private File getTestCapturePath(int counter) {
        return new File("captures", counter + ".png");
    }

    @Before
    public void setUp() {
        // dummy screen capture handler for this test
        AdapterContainer.globalInstance().setScreenCaptureAdapter(new ScreenCaptureAdapter() {

            @Override
            public byte[] captueScreen() {
                File captureFile = getTestCapturePath(counter);
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

}
