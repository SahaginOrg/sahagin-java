package multiextendstest;

import org.sahagin.runlib.external.TestDoc;

import base.Java6TestBase;

public class Base extends Java6TestBase {

    @TestDoc("Doc: baseMethod")
    public void baseMethod() {
        System.out.println("baseMethod");
    }
}
