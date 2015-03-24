package base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.sahagin.runlib.external.adapter.AdapterContainer;
import org.sahagin.runlib.external.adapter.ScreenCaptureAdapter;

public class Java6TestBase {
    private int counter = 1;

    private File getTestCapturePath(int counter) {
        return new File("captures", counter + ".png");
    }

    protected int nextCounter() {
        return counter;
    }

    @Before
    public void setUp() {
        // dummy screen capture handler for this test
        AdapterContainer.globalInstance().setScreenCaptureAdapter(new ScreenCaptureAdapter() {

            @Override
            public byte[] captureScreen() {
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
}
