package org.sahagin.runlib.srctreegen.SrcTreeGeneratorTestRes.defaultLocale;

import org.sahagin.runlib.external.Locale;
import org.sahagin.runlib.external.Page;
import org.sahagin.runlib.external.Pages;
import org.sahagin.runlib.external.TestDoc;
import org.sahagin.runlib.external.TestDocs;

@Pages({
    @Page(value = "Pages:ja-JP", locale = Locale.JA_JP),
    @Page(value = "Pages:default"),
})
public class TestMain {

    @TestDoc(value = "Doc:ja-JP", locale = Locale.JA_JP)
    public void jaJpTestDocMethod() {}

    @TestDoc(value ="Doc:default")
    public void defaultTestDocMethod() {}

    @TestDoc(value = "Doc:en-US", locale = Locale.EN_US)
    public void enUsTestDocMethod() {}

    @TestDocs({
        @TestDoc(value = "Docs:ja-JP", locale = Locale.JA_JP),
        @TestDoc(value = "Docs:default"),
    })
    public void testDocsMethod() {}

}
