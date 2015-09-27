package org.sahagin.runlib.srctreegen.SrcTreeGeneratorTestRes.defaultLocale.input;
import org.sahagin.runlib.external.Locale;
import org.sahagin.runlib.external.PageDoc;
import org.sahagin.runlib.external.PageDocs;
import org.sahagin.runlib.external.TestDoc;
import org.sahagin.runlib.external.TestDocs;

@PageDocs({
    @PageDoc(value = "Pages:ja-JP", locale = Locale.JA_JP),
    @PageDoc(value = "Pages:default"),
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
