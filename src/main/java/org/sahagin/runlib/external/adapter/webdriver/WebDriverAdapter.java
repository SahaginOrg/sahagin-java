package org.sahagin.runlib.external.adapter.webdriver;

import org.openqa.selenium.WebDriver;
import org.sahagin.runlib.external.adapter.Adapter;
import org.sahagin.runlib.external.adapter.AdapterContainer;

// TODO chromedriver has these problem (this is not sahagin problem, but the one of chromedriver)
// - cannot capture entire page
// - screen shot is executed asynchronously, so often fails to capture screen after action

public class WebDriverAdapter implements Adapter {

    @Override
    public void initialSetAdapter() {
        AdapterContainer container = AdapterContainer.globalInstance();
        container.addAdditionalTestDocsAdapter(new WebDriverAdditionalTestDocsAdapter());
    }

    @Override
    public String getName() {
        return "webDriver";
    }

    // can set null
    public static void setAdapter(final WebDriver driver) {
        AdapterContainer container = AdapterContainer.globalInstance();
        container.setScreenCaptureAdapter(new WebDriverScreenCaptureAdapter(driver));
    }
}
