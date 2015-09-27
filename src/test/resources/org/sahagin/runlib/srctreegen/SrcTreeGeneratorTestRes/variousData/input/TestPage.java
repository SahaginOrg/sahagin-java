package org.sahagin.runlib.srctreegen.SrcTreeGeneratorTestRes.variousData.input;

import org.sahagin.runlib.external.PageDoc;
import org.sahagin.runlib.external.TestDoc;

@PageDoc("Doc:TestPage")
public class TestPage {

    @TestDoc("Doc: TestPage")
    public TestPage() { }

    @TestDoc("data")
    public String getData() {
        return "EEE";
    }

    @TestDoc("Doc:argMethod:{arg1}:{arg2}:{0}:{1}:{this}")
    public void argMethod(String arg1, int arg2) {}

    @TestDoc("Doc:nest1:{arg}")
    public void nest1(String arg) {
        nest2(arg);
    }

    @TestDoc("Doc:nest2:{arg}")
    private void nest2(String arg) {
        nest3(arg);
    }

    @TestDoc("Doc:nest3:{arg}")
    private void nest3(String arg) {}

}
