package org.sahagin.runlib.srctreegen.SrcTreeGeneratorTestRes.implementsTest;

import org.sahagin.runlib.external.Page;
import org.sahagin.runlib.external.TestDoc;

@Page("Doc: ImplementsPage")
public class ImplementsPage implements ImplementsPageBase {

    @TestDoc("Doc: baseMethod: override: {arg}")
    @Override
    public void baseMethod(String arg) {}

    @TestDoc("Doc: childMethod: {arg}")
    public void childMethod(int arg) {}

}
