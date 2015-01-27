package org.sahagin.runlib.srctreegen.SrcTreeGeneratorTestRes.varLengthArray.input;

import org.junit.Test;
import org.sahagin.runlib.external.TestDoc;


public class TestMain {

    @Test
    public void test() {
        varLengthArgMethod(1, "A");
        varLengthArgMethod(1, "A", "B");
        varLengthArgMethod(1);
    }

    @TestDoc("Doc:{1}{2}")
    public void varLengthArgMethod(int arg1, String... arg2) {
        System.out.println(arg2);
    }
}
