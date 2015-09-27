package org.sahagin.runlib.srctreegen.SrcTreeGeneratorTestRes.implementsTest.input;

import org.sahagin.runlib.external.PageDoc;
import org.sahagin.runlib.external.TestDoc;

@PageDoc("Doc: ImplementsPageBase")
public interface ImplementsPageBase {

    @TestDoc("Doc: baseMethod: {arg}")
    public void baseMethod(String arg);

}
