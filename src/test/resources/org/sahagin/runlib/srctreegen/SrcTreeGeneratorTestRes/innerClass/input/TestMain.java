package org.sahagin.runlib.srctreegen.SrcTreeGeneratorTestRes.innerClass.input;

import org.junit.Test;
import org.sahagin.runlib.external.TestDoc;

public class TestMain {
    
    private static class MainInnerClass {

        @TestDoc("Doc:mainInnerClass")
        private void mainInnerClassMethod() {}

    }

    @Test
    public void test() {
        MainInnerClass mainInner = new MainInnerClass();
        mainInner.mainInnerClassMethod();
        TestSub.SubInnerClass subInner = new TestSub.SubInnerClass();
        subInner.subInnerClassMethod();
    }
}
