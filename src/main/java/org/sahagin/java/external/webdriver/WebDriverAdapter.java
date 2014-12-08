package org.sahagin.java.external.webdriver;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.sahagin.java.adapter.Adapter;
import org.sahagin.java.adapter.AdapterContainer;
import org.sahagin.java.adapter.AdditionalTestDocsAdapter;
import org.sahagin.java.adapter.ScreenCaptureAdapter;
import org.sahagin.java.additionaltestdoc.AdditionalTestDocs;

// TODO chromedriver has these problem (this is not sahagin problem, but the on of chromedriver)
// - cannot capture entire page
// - screen shot is executed asynchronously, so often fails to capture screen after action

public class WebDriverAdapter implements Adapter {

    @Override
    public void initialSetAdapter() {
        AdapterContainer container = AdapterContainer.globalInstance();
        container.addAdditionalTestDocsAdapter(new AdditionalTestDocsAdapterImpl());
    }

    // can set null
    public static void setAdapter(final WebDriver driver) {
        AdapterContainer container = AdapterContainer.globalInstance();
        container.setScreenCaptureAdapter(new ScreenCaptureAdapterImpl(driver));
    }

    public static class ScreenCaptureAdapterImpl implements ScreenCaptureAdapter {
        private WebDriver driver;

        public ScreenCaptureAdapterImpl(WebDriver driver) {
            this.driver = driver;
        }

        @Override
        public byte[] captueScreen() {
            if (driver == null) {
                return null;
            }
            if (!(driver instanceof TakesScreenshot)) {
                return null;
            }
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        }

    }

    private static class AdditionalTestDocsAdapterImpl implements AdditionalTestDocsAdapter {

        @Override
        public void classAdd(AdditionalTestDocs docs) {}

        @Override
        public void funcAdd(AdditionalTestDocs docs) {
            // TODO cannot handle methods defined on subclass
            // TODO multiple language support
            docs.methodAdd("org.openqa.selenium.WebDriver", "get", "「{0}」にページ遷移");
        }
    }

}
