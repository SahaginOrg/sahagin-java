package org.sahagin.runlib.external.adapter;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.sahagin.runlib.additionaltestdoc.AdditionalMethodTestDoc;
import org.sahagin.runlib.additionaltestdoc.AdditionalTestDocs;
import org.sahagin.runlib.external.CaptureStyle;
import org.sahagin.runlib.external.Locale;
import org.sahagin.runlib.external.adapter.junit4.JUnit4Adapter;
import org.sahagin.runlib.external.adapter.webdriver.WebDriverAdapter;
import org.sahagin.share.AcceptableLocales;

public class AdapterContainerTest {

    @Test
    public void testDocSetUpByEnUs() {
        AcceptableLocales locales = AcceptableLocales.getInstance(Locale.EN_US);
        JavaAdapterContainer.globalInitialize(locales, new JUnit4Adapter().getName());
        new JUnit4Adapter().initialSetAdapter();
        new WebDriverAdapter().initialSetAdapter();
        AdditionalTestDocs testDocs
        = AdapterContainer.globalInstance().getAdditionalTestDocs();

        AdditionalMethodTestDoc clickTestDoc
        = testDocs.getMethodTestDoc("org.openqa.selenium.WebElement", "click", new ArrayList<String>(0));
        assertThat(clickTestDoc.getTestDoc(), is("click {this}"));

        AdditionalMethodTestDoc isTestDoc1
        = testDocs.getMethodTestDoc("org.hamcrest.CoreMatchers", "is",
                Arrays.asList(Object.class.getSimpleName()));
        assertThat(isTestDoc1.getCaptureStyle(), is(CaptureStyle.NONE));
        assertThat(isTestDoc1.getTestDoc(), is("equals to '{0}'"));

        AdditionalMethodTestDoc isTestDoc2
        = testDocs.getMethodTestDoc("org.hamcrest.CoreMatchers", "is",
                Arrays.asList(Matcher.class.getCanonicalName()));
        assertThat(isTestDoc2.getCaptureStyle(), is(CaptureStyle.NONE));
        assertThat(isTestDoc2.getTestDoc(), is("{0}"));
    }

    @Test
    public void testDocSetUpByJaJp() {
        AcceptableLocales locales = AcceptableLocales.getInstance(Locale.JA_JP);
        JavaAdapterContainer.globalInitialize(locales, new JUnit4Adapter().getName());
        AdditionalTestDocs testDocs
        = AdapterContainer.globalInstance().getAdditionalTestDocs();
        testDocs.clear();
        new JUnit4Adapter().initialSetAdapter();
        new WebDriverAdapter().initialSetAdapter();

        AdditionalMethodTestDoc clickTestDoc
        = testDocs.getMethodTestDoc("org.openqa.selenium.WebElement", "click", new ArrayList<String>(0));
        assertThat(clickTestDoc.getTestDoc(), is("{this}をクリック"));
    }
}
