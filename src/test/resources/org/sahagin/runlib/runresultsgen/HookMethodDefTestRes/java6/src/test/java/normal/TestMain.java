package normal;

import static org.junit.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.sahagin.runlib.external.CaptureStyle;
import org.sahagin.runlib.external.TestDoc;

import base.Java6TestBase;

public class TestMain extends Java6TestBase {

    @Test
    public void successTest() {
        assertEquals(1, 1);
        assertThat(1 + 1, is(2));
    }

    @Test
    public void stepInCaptureTest() {
        noStepInCaptureMethod();
        stepInCaptureMethod();
    }

    @TestDoc(value = "Doc: noStepInCaptureMethod")
    public void noStepInCaptureMethod() {
        assertEquals(2, 2);
        assertEquals(2, 2);
    }

    @TestDoc(value = "Doc: stepInCaptureMethod", capture = CaptureStyle.STEP_IN)
    public void stepInCaptureMethod() {
        assertEquals(2, 2);
        assertEquals(2, 2);
    }

    @Test
    public void testDocMethodFailTest() {
        testDocMethod();
    }

    @TestDoc("Doc: testDocMethod")
    public void testDocMethod() {
        assertEquals(1, 2); // make test fail
    }

    @Test
    public void noTestDocMethodFailTest() {
        noTestDocMethod();
    }

    public void noTestDocMethod() {
        assertEquals(1, 2); // make test fail
    }

    @Test
    public void innerClassTest() {
        InnerClass instance = new InnerClass();
        instance.methodCall();
    }

    public class InnerClass {

        @TestDoc("Doc: methodCall")
        public void methodCall() {}

    }

    @Test
    public void anonymousClassTest() {
        anonymousCall(new AnonymousInterface() {
            @Override
            public void methodCall() {}
        });
    }

    public interface AnonymousInterface {

        @TestDoc("Doc: methodCall")
        void methodCall();
    }

    @TestDoc("Doc: anonymousCall")
    public void anonymousCall(AnonymousInterface instance) {
        instance.methodCall();
    }

    @Test
    public void multiLineStatementTest() {
        assertThat(
                1, is(1));
    }

    @Test
    public void multiStatementInALineTest() {
        assertThat(
                1, is(1));assertThat(
                        1, is(1));assertThat(
                                1, is(1));
        assertThat(1, is(1));assertThat(1, is(1));
    }

    @TestDoc("Doc: strReturnMethod")
    public String strReturnMethod(String str) {
        return str;
    }

    @Test
    public void localVarTest() {
        String str1 = "test";
        String str2 = strReturnMethod(str1);
        int int1 = str2.length();
    }

}
