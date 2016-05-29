package org.sahagin.runlib.srctreegen.SrcTreeGeneratorTestRes.exceptionhandler.input;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestMain {

    @Test
    public void tryFinallyTest() {
        try {
            assertEquals(1, 1);
        } catch (RuntimeException e) {
            throw e;
        } finally {
            assertEquals(2, 2);
        }
    }

    @Test
    public void multiCatchTest() {
        try {
            assertEquals(1, 1);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            // ignore
        }
    }
}
