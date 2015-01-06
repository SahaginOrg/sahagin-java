package org.sahagin.runlib.external.adapter;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.sahagin.runlib.additionaltestdoc.AdditionalFuncTestDoc;
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
        AdditionalFuncTestDoc assertThatTestDoc
        = testDocs.getFuncTestDoc("org.junit.Assert.assertThat");
        AdditionalFuncTestDoc clickTestDoc
        = testDocs.getFuncTestDoc("org.openqa.selenium.WebElement.click");
        assertThat(assertThatTestDoc.getCaptureStyle(), is(CaptureStyle.THIS_LINE));
        assertThat(assertThatTestDoc.getTestDoc(), is("check if \"{0}\" {1}"));
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
        AdditionalFuncTestDoc assertThatTestDoc
        = testDocs.getFuncTestDoc("org.junit.Assert.assertThat");
        AdditionalFuncTestDoc clickTestDoc
        = testDocs.getFuncTestDoc("org.openqa.selenium.WebElement.click");
        assertThat(assertThatTestDoc.getCaptureStyle(), is(CaptureStyle.THIS_LINE));
        assertThat(assertThatTestDoc.getTestDoc(), is("「{0}」が{1}ことをチェック"));
        assertThat(clickTestDoc.getTestDoc(), is("{this}をクリック"));
    }
}
