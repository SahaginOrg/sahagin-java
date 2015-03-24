package org.sahagin.runlib.external.adapter.selendroid;

import org.openqa.selenium.WebDriver;
import org.sahagin.runlib.external.adapter.Adapter;
import org.sahagin.runlib.external.adapter.AdapterContainer;
import org.sahagin.runlib.external.adapter.ResourceAdditionalTestDocsAdapter;
import org.sahagin.runlib.external.adapter.webdriver.WebDriverScreenCaptureAdapter;
import org.sahagin.share.CommonPath;

public class SelendroidAdapter implements Adapter {

    @Override
    public void initialSetAdapter() {
        AdapterContainer container = AdapterContainer.globalInstance();
        container.addAdditionalTestDocsAdapter(new AdditionalTestDocsAdapterImpl());
    }

    // can set null
    public static void setAdapter(final WebDriver driver) {
        AdapterContainer container = AdapterContainer.globalInstance();
        container.setScreenCaptureAdapter(new WebDriverScreenCaptureAdapter(driver));
    }

    private static class AdditionalTestDocsAdapterImpl extends
            ResourceAdditionalTestDocsAdapter {

        @Override
        public String resourceDirPath() {
            return CommonPath.standardAdapdaterLocaleResDirPath() + "/selendroid";
        }

        @Override
        public void classAdd() {
        }

        @Override
        public void methodAdd() {
            // in alphabetical order
        }

    }

}
