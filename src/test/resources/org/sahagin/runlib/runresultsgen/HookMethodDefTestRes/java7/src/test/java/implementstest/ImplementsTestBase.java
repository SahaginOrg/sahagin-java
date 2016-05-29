package implementstest;

import org.sahagin.runlib.external.TestDoc;

@TestDoc("Doc: ImplementsTestBase")
public interface ImplementsTestBase {

    @TestDoc("Doc: testBaseMethod: {arg}")
    public void testBaseMethod(String arg);
}
