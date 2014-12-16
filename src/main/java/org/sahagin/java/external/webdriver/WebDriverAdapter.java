package org.sahagin.java.external.webdriver;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.SessionNotFoundException;
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
            try {
                return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            } catch (SessionNotFoundException e) {
                // just do nothing if WebDriver instance is in invalid state
                return null;
            }
        }

    }

    private static class AdditionalTestDocsAdapterImpl implements AdditionalTestDocsAdapter {

        @Override
        public void classAdd(AdditionalTestDocs docs) {}

        @Override
        public void funcAdd(AdditionalTestDocs docs) {
            // TODO cannot handle methods defined on subclass?
            // TODO multiple language support

            // alphabetical order

            docs.methodAdd("org.openqa.selenium.By", "className", "クラス名 = {0}");
            docs.methodAdd("org.openqa.selenium.By", "cssSelector", "css = {0}");
            docs.methodAdd("org.openqa.selenium.By", "id", "id = {0}");
            docs.methodAdd("org.openqa.selenium.By", "linkText", "テキスト = {0}");
            docs.methodAdd("org.openqa.selenium.By", "name", "name = {0}");
            docs.methodAdd("org.openqa.selenium.By", "partialLinkText", "テキスト = {0}(部分一致)");
            docs.methodAdd("org.openqa.selenium.By", "tagName", "タグ名 = {0}");
            docs.methodAdd("org.openqa.selenium.By", "xpath", "xpath = {0}");
            docs.methodAdd("org.openqa.selenium.WebDriver", "findElement", "要素「{0}」");
            docs.methodAdd("org.openqa.selenium.WebDriver", "get", "「{0}」にページ遷移");
            docs.methodAdd("org.openqa.selenium.WebElement", "clear", "{this}のテキストをクリア");
            docs.methodAdd("org.openqa.selenium.WebElement", "click", "{this}をクリック");
            docs.methodAdd("org.openqa.selenium.WebElement", "getAttribute", "{this}の属性「{0}」の値");
            docs.methodAdd("org.openqa.selenium.WebElement", "getText", "{this}の表示テキスト");
            docs.methodAdd("org.openqa.selenium.WebElement", "isSelected", "{this}が選択されているか");
            docs.methodAdd("org.openqa.selenium.WebElement", "sendKeys", "{this}に「{0}」を入力");
        }
    }

}
