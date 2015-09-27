package org.sahagin.runlib.srctreegen.SrcTreeGeneratorTestRes.localVar.input;

import org.junit.Test;
import org.sahagin.runlib.external.PageDoc;
import org.sahagin.runlib.external.TestDoc;

public class TestMain {

    @PageDoc("Doc: Page")
    public static class SubPage {

        @TestDoc("Doc:SubPage")
        public SubPage() {}

        @TestDoc("Doc:generate")
        public static SubPage generate() {
            return new SubPage();
        }

        @TestDoc("Doc:get")
        public int get() {
            return 0;
        }
    }

    @TestDoc("Doc:String SubMehtod")
    public void subMethod(String arg1, String arg2) {}

    @TestDoc("Doc:int SubMehtod")
    public void subMethod(int arg1, int arg2) {}

    @TestDoc("Doc:SubPage SubMehtod")
    public void subMethod(SubPage arg1, SubPage arg2) {}

    @Test
    public void testMethod() {
        // assign non-UnknownCode
        String str1 = "A";
        String str2;
        str1 = "B";
        str2 = "C";
        subMethod(str1, str2);

        // assign UnknownCode
        int int1 = 1;
        int int2;
        int1 = 2;
        int2 = 3;
        subMethod(int1, int2);

        // assign Page
        SubPage page1 = new SubPage();
        SubPage page2 = SubPage.generate();
        subMethod(page1, page2);

        // assign MethodInvoke
        int int3 = page1.get();
        subMethod(int3, int3);
    }
}
