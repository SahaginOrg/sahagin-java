package org.sahagin.runlib.srctreegen.SrcTreeGeneratorTestRes.extendsTest.input;

import org.sahagin.runlib.external.Page;
import org.sahagin.runlib.external.Pages;
import org.sahagin.runlib.external.TestDoc;
import org.sahagin.runlib.external.TestDocs;

@Pages({@Page("Doc: ExtendsPage")})
public class ExtendsPage extends ExtendsPageBase {

    @TestDoc("Doc: overriddenMethod: override: {arg}")
    @Override
    public void overriddenMethod(String arg) {
        super.overriddenMethod(arg);
    }

    @TestDocs({@TestDoc("Doc: abstractMethod: override: {arg}")})
    @Override
    public void abstractMethod(int arg) {}


}
