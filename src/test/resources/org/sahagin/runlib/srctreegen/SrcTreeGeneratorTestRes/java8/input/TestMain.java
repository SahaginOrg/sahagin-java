package org.sahagin.runlib.srctreegen.SrcTreeGeneratorTestRes.java8.input;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.sahagin.runlib.external.TestDoc;

public class TestMain {

    @Test
    public void streamApiCallTest() {
        List<String> slist = new ArrayList<String>(2);
        slist.add("a");
        slist.stream().forEach(s -> System.out.println(s));
        slist.stream().forEach(s -> print(s));
    }

    @TestDoc("Doc: print")
    private void print(String str) {}

    @Test
    public void defaultInterfaceTest() {
        InterfaceWithDefault instance = new InterfaceWithDefault() {};
        instance.defaultMethod();
    }

    private interface InterfaceWithDefault {

        @TestDoc("Doc: defaultMethod")
        default void defaultMethod() {}
    }

}
