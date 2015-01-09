package org.sahagin.runlib.srctreegen.SrcTreeGeneratorTestRes.implementsTest.input;

import org.sahagin.runlib.external.Page;
import org.sahagin.runlib.external.TestDoc;

@Page("Doc: ImplementsPageBase")
public interface ImplementsPageBase {

    @TestDoc("Doc: baseMethod: {arg}")
    public void baseMethod(String arg);

}
