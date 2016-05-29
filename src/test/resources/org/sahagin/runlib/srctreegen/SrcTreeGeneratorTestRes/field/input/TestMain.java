package org.sahagin.runlib.srctreegen.SrcTreeGeneratorTestRes.field.input;

import org.junit.Test;
import org.sahagin.runlib.external.TestDoc;

public class TestMain {
    @TestDoc("Doc: field1")
    private String field1;
    @TestDoc("Doc: field2")
    private int field2 = 2;

    @Test
    public void test() {
        String localVar1 = field1;
        int localVar2 = field2;
        System.out.println(localVar1 + localVar2);
    }
}
