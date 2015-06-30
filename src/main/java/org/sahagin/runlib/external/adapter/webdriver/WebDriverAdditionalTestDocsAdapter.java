package org.sahagin.runlib.external.adapter.webdriver;

import org.sahagin.runlib.external.CaptureStyle;
import org.sahagin.runlib.external.adapter.ResourceAdditionalTestDocsAdapter;
import org.sahagin.share.CommonPath;

public class WebDriverAdditionalTestDocsAdapter extends ResourceAdditionalTestDocsAdapter {

    @Override
    public String resourceDirPath() {
        return CommonPath.standardAdapdaterLocaleResDirPath("java") + "/webdriver";
    }

    @Override
    public void classAdd() {
    }

    @Override
    public void methodAdd() {
        // in alphabetical order
        methodAdd("org.openqa.selenium.By", "className", CaptureStyle.NONE);
        methodAdd("org.openqa.selenium.By", "cssSelector", CaptureStyle.NONE);
        methodAdd("org.openqa.selenium.By", "id", CaptureStyle.NONE);
        methodAdd("org.openqa.selenium.By", "linkText", CaptureStyle.NONE);
        methodAdd("org.openqa.selenium.By", "name", CaptureStyle.NONE);
        methodAdd("org.openqa.selenium.By", "partialLinkText", CaptureStyle.NONE);
        methodAdd("org.openqa.selenium.By", "tagName", CaptureStyle.NONE);
        methodAdd("org.openqa.selenium.By", "xpath", CaptureStyle.NONE);
        methodAdd("org.openqa.selenium.remote.RemoteWebDriver", "findElementByClassName");
        methodAdd("org.openqa.selenium.remote.RemoteWebDriver", "findElementByCssSelector");
        methodAdd("org.openqa.selenium.remote.RemoteWebDriver", "findElementById");
        methodAdd("org.openqa.selenium.remote.RemoteWebDriver", "findElementByLinkText");
        methodAdd("org.openqa.selenium.remote.RemoteWebDriver", "findElementByName");
        methodAdd("org.openqa.selenium.remote.RemoteWebDriver", "findElementByPartialLinkText");
        methodAdd("org.openqa.selenium.remote.RemoteWebDriver", "findElementByTagName");
        methodAdd("org.openqa.selenium.remote.RemoteWebDriver", "findElementByXPath");
        methodAdd("org.openqa.selenium.remote.RemoteWebDriver", "findElementsByClassName");
        methodAdd("org.openqa.selenium.remote.RemoteWebDriver", "findElementsByCssSelector");
        methodAdd("org.openqa.selenium.remote.RemoteWebDriver", "findElementsById");
        methodAdd("org.openqa.selenium.remote.RemoteWebDriver", "findElementsByLinkText");
        methodAdd("org.openqa.selenium.remote.RemoteWebDriver", "findElementsByName");
        methodAdd("org.openqa.selenium.remote.RemoteWebDriver", "findElementsByPartialLinkText");
        methodAdd("org.openqa.selenium.remote.RemoteWebDriver", "findElementsByTagName");
        methodAdd("org.openqa.selenium.remote.RemoteWebDriver", "findElementsByXPath");
        methodAdd("org.openqa.selenium.Rotatable", "getOrientation");
        methodAdd("org.openqa.selenium.Rotatable", "rotate");
        methodAdd("org.openqa.selenium.support.ui.Select", "Select");
        methodAdd("org.openqa.selenium.support.ui.Select", "selectByIndex");
        methodAdd("org.openqa.selenium.support.ui.Select", "selectByValue");
        methodAdd("org.openqa.selenium.support.ui.Select", "selectByVisibleText");
        methodAdd("org.openqa.selenium.WebDriver", "close");
        methodAdd("org.openqa.selenium.WebDriver", "findElement");
        methodAdd("org.openqa.selenium.WebDriver", "findElements");
        methodAdd("org.openqa.selenium.WebDriver", "get");
        methodAdd("org.openqa.selenium.WebDriver", "getCurrentUrl");
        methodAdd("org.openqa.selenium.WebDriver", "getTitle");
        methodAdd("org.openqa.selenium.WebDriver", "quit");
        methodAdd("org.openqa.selenium.WebDriver$Navigation", "back");
        methodAdd("org.openqa.selenium.WebDriver$Navigation", "forward");
        methodAdd("org.openqa.selenium.WebDriver$Navigation", "refresh");
        methodAdd("org.openqa.selenium.WebDriver$Navigation", "to");
        methodAdd("org.openqa.selenium.WebElement", "clear");
        methodAdd("org.openqa.selenium.WebElement", "click");
        methodAdd("org.openqa.selenium.WebElement", "getAttribute");
        methodAdd("org.openqa.selenium.WebElement", "getText");
        methodAdd("org.openqa.selenium.WebElement", "isDisplayed");
        methodAdd("org.openqa.selenium.WebElement", "isEnabled");
        methodAdd("org.openqa.selenium.WebElement", "isSelected");
        methodAdd("org.openqa.selenium.WebElement", "sendKeys");
        methodAdd("org.openqa.selenium.WebElement", "submit");
    }

}