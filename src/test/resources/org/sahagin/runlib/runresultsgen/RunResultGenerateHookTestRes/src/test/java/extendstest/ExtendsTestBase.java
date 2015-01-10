package extendstest;

import org.sahagin.runlib.external.TestDoc;
import base.TestBase;

@TestDoc("Doc: ExtendsTestBase")
public abstract class ExtendsTestBase extends TestBase {

    @TestDoc("Doc: testBaseMethod: {arg}")
    public void testBaseMethod(String arg) {}

}
