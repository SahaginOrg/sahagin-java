package org.sahagin.runlib.external.adapter.fluentlenium;

import org.fluentlenium.core.Fluent;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.SessionNotFoundException;
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

    // can set null
    public static void setAdapter(Fluent fluent) {
        AdapterContainer container = AdapterContainer.globalInstance();
        container.setScreenCaptureAdapter(new ScreenCaptureAdapterImpl(fluent));
    }

    public static class ScreenCaptureAdapterImpl implements
            ScreenCaptureAdapter {
        private Fluent fluent;

        public ScreenCaptureAdapterImpl(Fluent fluent) {
            this.fluent = fluent;
        }

        @Override
        public byte[] captueScreen() {
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
            } catch (SessionNotFoundException e) {
                // just do nothing if WebDriver instance is in invalid state
                return null;
            }
        }

    }

    private static class AdditionalTestDocsAdapterImpl extends
            ResourceAdditionalTestDocsAdapter {

        @Override
        public String resourceDirPath() {
            return CommonPath.standardAdapdaterLocaleResDirPath()
                    + "/fluentlenium";
        }

        @Override
        public void classAdd() {}

        @Override
        public void methodAdd() {
            // TODO cannot handle methods defined on subclass?

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
            methodAdd("org.fluentlenium.core.filter.FilterConstructor", "withClass", "String");
            methodAdd("org.fluentlenium.core.filter.FilterConstructor", "withName", "String");
            methodAdd("org.fluentlenium.core.filter.FilterConstructor", "withText", "String");
            methodAdd("org.fluentlenium.core.Fluent", "$", "String,org.fluentlenium.core.filter.Filter[]", 1);
            methodAdd("org.fluentlenium.core.Fluent", "$", "String,java.lang.Integer,org.fluentlenium.core.filter.Filter[]", 2);
            methodAdd("org.fluentlenium.core.Fluent", "clear", null, 1);
            methodAdd("org.fluentlenium.core.Fluent", "click", null, 1);
            methodAdd("org.fluentlenium.core.Fluent", "executeScript", null, 1);
            methodAdd("org.fluentlenium.core.Fluent", "fill", null, 1);
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
