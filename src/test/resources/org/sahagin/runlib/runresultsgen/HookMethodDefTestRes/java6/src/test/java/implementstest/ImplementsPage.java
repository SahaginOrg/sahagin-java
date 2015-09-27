package implementstest;

import org.sahagin.runlib.external.PageDoc;
import org.sahagin.runlib.external.TestDoc;

@PageDoc("Doc: ImplementsPage")
public class ImplementsPage implements ImplementsPageBase {

    @TestDoc("Doc: baseMethod: override: {arg}")
    @Override
    public void baseMethod(String arg) {}

    @TestDoc("Doc: childMethod: {arg}")
    public void childMethod(int arg) {}

}
