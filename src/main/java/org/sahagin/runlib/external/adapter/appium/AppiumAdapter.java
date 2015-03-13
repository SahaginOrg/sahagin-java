package org.sahagin.runlib.external.adapter.appium;

import org.openqa.selenium.WebDriver;
import org.sahagin.runlib.external.CaptureStyle;
import org.sahagin.runlib.external.adapter.Adapter;
import org.sahagin.runlib.external.adapter.AdapterContainer;
import org.sahagin.runlib.external.adapter.ResourceAdditionalTestDocsAdapter;
import org.sahagin.runlib.external.adapter.webdriver.WebDriverScreenCaptureAdapterImpl;
import org.sahagin.share.CommonPath;

public class AppiumAdapter implements Adapter {

    @Override
    public void initialSetAdapter() {
        AdapterContainer container = AdapterContainer.globalInstance();
        container.addAdditionalTestDocsAdapter(new AdditionalTestDocsAdapterImpl());
    }

    // can set null
    public static void setAdapter(final WebDriver driver) {
        AdapterContainer container = AdapterContainer.globalInstance();
        container.setScreenCaptureAdapter(new WebDriverScreenCaptureAdapterImpl(driver));
    }

    private static class AdditionalTestDocsAdapterImpl extends
            ResourceAdditionalTestDocsAdapter {

        @Override
        public String resourceDirPath() {
            return CommonPath.standardAdapdaterLocaleResDirPath()
                    + "/appium";
        }

        @Override
        public void classAdd() {
        }

        @Override
        public void methodAdd() {
            // in alphabetical order
            methodAdd("io.appium.java_client.android.AndroidDriver", "findElementByAndroidUIAutomator");
            methodAdd("io.appium.java_client.android.AndroidDriver", "findElementsByAndroidUIAutomator");
            methodAdd("io.appium.java_client.android.AndroidDriver", "isLocked");
            methodAdd("io.appium.java_client.AppiumDriver", "findElementByAccessibilityId");
            methodAdd("io.appium.java_client.AppiumDriver", "findElementsByAccessibilityId");
            methodAdd("io.appium.java_client.AppiumDriver", "getOrientation");
            methodAdd("io.appium.java_client.AppiumDriver", "hideKeyboard");
            methodAdd("io.appium.java_client.AppiumDriver", "lockScreen");
            methodAdd("io.appium.java_client.AppiumDriver", "pinch", "int,int");
            methodAdd("io.appium.java_client.AppiumDriver", "pinch", "org.openqa.selenium.WebElement");
            methodAdd("io.appium.java_client.AppiumDriver", "swipe");
            methodAdd("io.appium.java_client.AppiumDriver", "tap", "int,int,int,int");
            methodAdd("io.appium.java_client.AppiumDriver", "tap", "int,org.openqa.selenium.WebElement,int");
            methodAdd("io.appium.java_client.AppiumDriver", "zoom", "int,int");
            methodAdd("io.appium.java_client.AppiumDriver", "zoom", "org.openqa.selenium.WebElement");
            methodAdd("io.appium.java_client.ios.IOSDriver", "findElementByIosUIAutomation");
            methodAdd("io.appium.java_client.ios.IOSDriver", "findElementsByIosUIAutomation");
            methodAdd("io.appium.java_client.ios.IOSDriver", "shake");
            methodAdd("io.appium.java_client.MobileBy", "AccessibilityId", CaptureStyle.NONE);
            methodAdd("io.appium.java_client.MobileBy", "AndroidUIAutomator", CaptureStyle.NONE);
            methodAdd("io.appium.java_client.MobileBy", "IosUIAutomation", CaptureStyle.NONE);
        }

    }

}

