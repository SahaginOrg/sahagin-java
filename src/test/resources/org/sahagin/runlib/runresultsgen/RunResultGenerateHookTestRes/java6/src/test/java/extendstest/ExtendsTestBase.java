package extendstest;

import org.sahagin.runlib.external.TestDoc;

import base.Java6TestBase;

@TestDoc("Doc: ExtendsTestBase")
public abstract class ExtendsTestBase extends Java6TestBase {

    @TestDoc("Doc: testBaseMethod: {arg}")
    public void testBaseMethod(String arg) {}

}
