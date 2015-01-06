package org.sahagin.runlib.external.adapter.webdriver;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.SessionNotFoundException;
import org.sahagin.runlib.external.adapter.Adapter;
import org.sahagin.runlib.external.adapter.AdapterContainer;
import org.sahagin.runlib.external.adapter.ResourceAdditionalTestDocsAdapter;
import org.sahagin.runlib.external.adapter.ScreenCaptureAdapter;
import org.sahagin.share.CommonPath;

// TODO chromedriver has these problem (this is not sahagin problem, but the one of chromedriver)
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
            try {
                return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            } catch (SessionNotFoundException e) {
                // just do nothing if WebDriver instance is in invalid state
                return null;
            }
        }

    }

    private static class AdditionalTestDocsAdapterImpl extends ResourceAdditionalTestDocsAdapter {

		@Override
		public String resourceDirPath() {
			return CommonPath.standardAdapdaterLocaleResDirPath() + "/webdriver";
		}

        @Override
        public void classAdd() {}

        @Override
        public void funcAdd() {
            // TODO cannot handle methods defined on subclass?

            // in alphabetical order
            methodAdd("org.openqa.selenium.By", "className");
            methodAdd("org.openqa.selenium.By", "cssSelector");
            methodAdd("org.openqa.selenium.By", "id");
            methodAdd("org.openqa.selenium.By", "linkText");
            methodAdd("org.openqa.selenium.By", "name");
            methodAdd("org.openqa.selenium.By", "partialLinkText");
            methodAdd("org.openqa.selenium.By", "tagName");
            methodAdd("org.openqa.selenium.By", "xpath");
            methodAdd("org.openqa.selenium.WebDriver", "findElement");
            methodAdd("org.openqa.selenium.WebDriver", "get");
            methodAdd("org.openqa.selenium.WebElement", "clear");
            methodAdd("org.openqa.selenium.WebElement", "click");
            methodAdd("org.openqa.selenium.WebElement", "getAttribute");
            methodAdd("org.openqa.selenium.WebElement", "getText");
            methodAdd("org.openqa.selenium.WebElement", "isSelected");
            methodAdd("org.openqa.selenium.WebElement", "sendKeys");
        }
        
    }

}
