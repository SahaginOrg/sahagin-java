package org.sahagin.runlib.srctreegen.SrcTreeGeneratorTestRes.recurseMethodCall.input;

import org.sahagin.runlib.external.TestDoc;

public class TestMain {

    @TestDoc("Doc: recurseMethodCall")
    public void recurseMethodCall() {
        recurseMethodCall();
    }

    @TestDoc("Doc: mutualRecurseMethodCall1")
    public void mutualRecurseMethodCall1() {
        mutualRecurseMethodCall2();
    }

    @TestDoc("Doc: mutualRecurseMethodCall2")
    public void mutualRecurseMethodCall2() {
        mutualRecurseMethodCall1();
    }

}
