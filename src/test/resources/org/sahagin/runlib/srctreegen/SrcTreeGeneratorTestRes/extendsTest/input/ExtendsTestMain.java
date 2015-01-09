package org.sahagin.runlib.srctreegen.SrcTreeGeneratorTestRes.extendsTest;

import org.junit.Test;
import org.sahagin.runlib.external.CaptureStyle;
import org.sahagin.runlib.external.TestDoc;

@TestDoc("Doc: ExtendsTestMain")
public class ExtendsTestMain extends ExtendsTestBase {

    @TestDoc(value = "Doc: testBaseMethod: override: {arg}", capture = CaptureStyle.STEP_IN)
    @Override
    public void testBaseMethod(String arg) {
        super.testBaseMethod(arg);
    }

    @Test
    public void extendsTest() {
        testBaseMethod("ABC");
        ExtendsPage pageInstance = new ExtendsPage();
        pageInstance.abstractMethod(123);
        pageInstance.finalMethod(null);
        pageInstance.overriddenMethod("YYY");
        ExtendsPageBase baseInstance = new ExtendsPage();
        baseInstance.abstractMethod(456);
        baseInstance.finalMethod("WWW");
        baseInstance.overriddenMethod("ZZZ");
    }
}
