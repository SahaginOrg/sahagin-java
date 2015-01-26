package org.sahagin.runlib.srctreegen.SrcTreeGeneratorTestRes.additionalTestDocs.input;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class TestMain {

    @Test
    public void test() {
        assertThat("ABC", is("ABC"));
        assertThat("ABC", is(not("DEF")));
    }

}
