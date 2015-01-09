package org.sahagin.runlib.srctreegen.SrcTreeGeneratorTestRes.implementsTest.input;

import org.junit.Test;

public class ImplementsTestMain implements ImplementsTestBase {

    @Override
    public void testBaseMethod(String arg) {}

    @Test
    public void implementsTest() {
        testBaseMethod("DEF");
        ImplementsPage pageInstance = new ImplementsPage();
        pageInstance.baseMethod("QQQ");
        pageInstance.childMethod(-1);
        ImplementsPageBase baseInstance = new ImplementsPage();
        baseInstance.baseMethod("RRR");
    }

}
