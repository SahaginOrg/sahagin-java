package org.sahagin.runlib.srctreegen.SrcTreeGeneratorTestRes.testStepLabel.input;

import org.junit.Test;
import org.sahagin.runlib.external.TestDoc;
import org.sahagin.runlib.external.TestStepLabelMethod;

import static org.sahagin.runlib.external.TestStepLabelMethod.*;

public class TestMain {

    @Test
    public void testMethod() {
        TestStepLabelMethod.TestDoc("Doc: Step1");
        System.out.println("123");
        TestDoc("Doc: Step2");
        subMethod();
        TestStepLabelMethod.TestDoc("Doc: Step3");
    }

    @TestDoc("Doc: subMethod")
    public void subMethod() {
        TestDoc("Doc: Nested Step1");
        TestDoc("Doc: Nested Step2");
        System.out.println("ABC");
        TestDoc("Doc: Nested Step3");
        System.out.println("DEF");
    }
}
