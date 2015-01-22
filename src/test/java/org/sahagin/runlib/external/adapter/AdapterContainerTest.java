package org.sahagin.runlib.external.adapter;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

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
        AdapterContainer.globalInitialize(locales);
        new JUnit4Adapter().initialSetAdapter();
        new WebDriverAdapter().initialSetAdapter();
        AdditionalTestDocs testDocs
        = AdapterContainer.globalInstance().getAdditionalTestDocs();
        AdditionalMethodTestDoc assertThatTestDoc
        = testDocs.getMethodTestDoc("org.junit.Assert.assertThat");
        AdditionalMethodTestDoc clickTestDoc
        = testDocs.getMethodTestDoc("org.openqa.selenium.WebElement.click");
        assertThat(assertThatTestDoc.getCaptureStyle(), is(CaptureStyle.THIS_LINE));
        assertThat(assertThatTestDoc.getTestDoc(), is("check that '{0}' {1}"));
        assertThat(clickTestDoc.getTestDoc(), is("click {this}"));
    }

    @Test
    public void testDocSetUpByJaJp() {
        AcceptableLocales locales = AcceptableLocales.getInstance(Locale.JA_JP);
        AdapterContainer.globalInitialize(locales);
        new JUnit4Adapter().initialSetAdapter();
        new WebDriverAdapter().initialSetAdapter();
        AdditionalTestDocs testDocs
        = AdapterContainer.globalInstance().getAdditionalTestDocs();
        AdditionalMethodTestDoc assertThatTestDoc
        = testDocs.getMethodTestDoc("org.junit.Assert.assertThat");
        AdditionalMethodTestDoc clickTestDoc
        = testDocs.getMethodTestDoc("org.openqa.selenium.WebElement.click");
        assertThat(assertThatTestDoc.getCaptureStyle(), is(CaptureStyle.THIS_LINE));
        assertThat(assertThatTestDoc.getTestDoc(), is("「{0}」が{1}ことをチェック"));
        assertThat(clickTestDoc.getTestDoc(), is("{this}をクリック"));
    }
}
