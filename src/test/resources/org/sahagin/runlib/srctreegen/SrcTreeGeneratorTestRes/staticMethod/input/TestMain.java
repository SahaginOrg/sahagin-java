package org.sahagin.runlib.srctreegen.SrcTreeGeneratorTestRes.staticMethod.input;

import org.junit.Test;

public class TestMain {

    @Test
    @SuppressWarnings("static-access")
    public void test() {
        TestSub.staticMethod();
        TestSub sub = new TestSub();
        sub.staticMethod();
    }
}
