package org.sahagin.runlib.external.adapter.fluentlenium;

import org.fluentlenium.core.FluentDriver;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.sahagin.runlib.external.CaptureStyle;
import org.sahagin.runlib.external.adapter.Adapter;
import org.sahagin.runlib.external.adapter.AdapterContainer;
import org.sahagin.runlib.external.adapter.ResourceAdditionalTestDocsAdapter;
import org.sahagin.runlib.external.adapter.ScreenCaptureAdapter;
import org.sahagin.share.CommonPath;

public class FluentLeniumAdapter implements Adapter {

    @Override
    public void initialSetAdapter() {
        AdapterContainer container = AdapterContainer.globalInstance();
        container.addAdditionalTestDocsAdapter(new AdditionalTestDocsAdapterImpl());
    }

    @Override
    public String getName() {
        return "fluentLenium";
    }

    // can set null
    public static void setAdapter(FluentDriver fluent) {
        AdapterContainer container = AdapterContainer.globalInstance();
        container.setScreenCaptureAdapter(new ScreenCaptureAdapterImpl(fluent));
    }

    private static class ScreenCaptureAdapterImpl implements
            ScreenCaptureAdapter {
        private FluentDriver fluent;

        public ScreenCaptureAdapterImpl(FluentDriver fluent) {
            this.fluent = fluent;
        }

        @Override
        public byte[] captureScreen() {
            if (fluent == null) {
                return null;
            }
            WebDriver driver = fluent.getDriver();
            if (driver == null) {
                return null;
            }
            if (!(driver instanceof TakesScreenshot)) {
                return null;
            }
            try {
                return ((TakesScreenshot) driver)
                        .getScreenshotAs(OutputType.BYTES);
            } catch (NoSuchSessionException e) {
                // just do nothing if WebDriver instance is in invalid state
                return null;
            }
        }

    }

    private static class AdditionalTestDocsAdapterImpl extends
            ResourceAdditionalTestDocsAdapter {

        @Override
        public String resourceDirPath() {
            return CommonPath.standardAdapdaterLocaleResDirPath("java") + "/fluentlenium";
        }

        @Override
        public void classAdd() {}

        @Override
        public void methodAdd() {
            // in alphabetical order
            methodAdd("org.fluentlenium.core.action.FillConstructor", "with");
            methodAdd("org.fluentlenium.core.domain.FluentList", "clear");
            methodAdd("org.fluentlenium.core.domain.FluentList", "click");
            methodAdd("org.fluentlenium.core.domain.FluentList", "getAttribute");
            methodAdd("org.fluentlenium.core.domain.FluentList", "getText");
            methodAdd("org.fluentlenium.core.domain.FluentList", "getValue");
            methodAdd("org.fluentlenium.core.domain.FluentWebElement", "clear");
            methodAdd("org.fluentlenium.core.domain.FluentWebElement", "click");
            methodAdd("org.fluentlenium.core.domain.FluentWebElement", "getAttribute");
            methodAdd("org.fluentlenium.core.domain.FluentWebElement", "getText");
            methodAdd("org.fluentlenium.core.domain.FluentWebElement", "getValue");
            methodAdd("org.fluentlenium.core.domain.FluentWebElement", "isDisplayed");
            methodAdd("org.fluentlenium.core.domain.FluentWebElement", "isEnabled");
            methodAdd("org.fluentlenium.core.domain.FluentWebElement", "isSelected");
            methodAdd("org.fluentlenium.core.filter.FilterConstructor", "withClass", "String", CaptureStyle.NONE);
            methodAdd("org.fluentlenium.core.filter.FilterConstructor", "withName", "String", CaptureStyle.NONE);
            methodAdd("org.fluentlenium.core.filter.FilterConstructor", "withText", "String", CaptureStyle.NONE);
            methodAdd("org.fluentlenium.core.Fluent", "$", "String,org.fluentlenium.core.filter.Filter[]", 1);
            methodAdd("org.fluentlenium.core.Fluent", "$", "String,java.lang.Integer,org.fluentlenium.core.filter.Filter[]", 2);
            methodAdd("org.fluentlenium.core.Fluent", "clear", 1);
            methodAdd("org.fluentlenium.core.Fluent", "click", 1);
            methodAdd("org.fluentlenium.core.Fluent", "executeScript", 1);
            methodAdd("org.fluentlenium.core.Fluent", "fill", 1);
            methodAdd("org.fluentlenium.core.Fluent", "find", "String,org.fluentlenium.core.filter.Filter[]", 1);
            methodAdd("org.fluentlenium.core.Fluent", "find", "String,java.lang.Integer,org.fluentlenium.core.filter.Filter[]", 2);
            methodAdd("org.fluentlenium.core.Fluent", "findFirst", "String,org.fluentlenium.core.filter.Filter[]", 1);
            methodAdd("org.fluentlenium.core.Fluent", "goTo", "String");
            methodAdd("org.fluentlenium.core.Fluent", "goTo", "org.fluentlenium.core.FluentPage");
            methodAdd("org.fluentlenium.core.Fluent", "takeScreenShot", "String");
            methodAdd("org.fluentlenium.core.Fluent", "title");
            methodAdd("org.fluentlenium.core.FluentPage", "go");
            methodAdd("org.fluentlenium.core.FluentPage", "isAt");
        }
    }
}
