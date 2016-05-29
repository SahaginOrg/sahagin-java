package multiextendstest;

import org.sahagin.runlib.external.TestDoc;

import base.Java7TestBase;

public class Base extends Java7TestBase {

    @TestDoc("Doc: baseMethod")
    public void baseMethod() {
        System.out.println("baseMethod");
    }
}
