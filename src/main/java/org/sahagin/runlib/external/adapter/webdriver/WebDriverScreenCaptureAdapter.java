package org.sahagin.runlib.external.adapter.webdriver;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.SessionNotFoundException;
import org.sahagin.runlib.external.adapter.ScreenCaptureAdapter;

public class WebDriverScreenCaptureAdapter implements ScreenCaptureAdapter {
    private WebDriver driver;

    public WebDriverScreenCaptureAdapter(WebDriver driver) {
        this.driver = driver;
    }

    @Override
    public byte[] captureScreen() {
        if (driver == null) {
            return null;
        }
        if (!(driver instanceof TakesScreenshot)) {
            return null;
        }
        try {
            return ((TakesScreenshot) driver)
                    .getScreenshotAs(OutputType.BYTES);
        } catch (SessionNotFoundException e) {
            // just do nothing if WebDriver instance is in invalid state
            return null;
        }
        // TODO test should not fail when taking screen capture fails?
    }

}