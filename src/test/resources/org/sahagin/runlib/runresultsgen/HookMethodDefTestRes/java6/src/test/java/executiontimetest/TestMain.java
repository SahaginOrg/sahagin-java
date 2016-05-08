package executiontimetest;

import org.junit.Test;
import org.sahagin.runlib.external.CaptureStyle;
import org.sahagin.runlib.external.TestDoc;

import base.Java6TestBase;

import static org.sahagin.runlib.external.TestStepLabelMethod.*;

// TODO error test execution time test

public class TestMain extends Java6TestBase {
    private int counter = 0;

    @TestDoc("*")
    public void sleep() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @TestDoc(value = "*", capture = CaptureStyle.STEP_IN)
    public void subMethod() {
        sleep();
        sleep();
    }

    @TestDoc("*")
    public void noStepInSubMethod() {
        sleep();
    }

    @TestDoc(value = "*", capture = CaptureStyle.STEP_IN)
    public void recurseSubMethod() {
        sleep();
        counter++;
        if (counter >= 3) {
            return;
        }
        recurseSubMethod();
    }

    @TestDoc(value = "*", capture = CaptureStyle.STEP_IN)
    public int returnSubMethod() {
        sleep();
        return 1;
    }

    @Test
    public void executionTimeTest() {
        subMethod();
        noStepInSubMethod();
        recurseSubMethod();
        returnSubMethod();
    }

    @TestDoc("*")
    public void stepSubMethod() {
        sleep();
    }

    @Test
    public void testStepLabelExecutionTimeTest() {
        TestDoc("step 1");
        stepSubMethod();
        stepSubMethod();
        TestDoc("step 2");
        stepSubMethod();
    }
}
