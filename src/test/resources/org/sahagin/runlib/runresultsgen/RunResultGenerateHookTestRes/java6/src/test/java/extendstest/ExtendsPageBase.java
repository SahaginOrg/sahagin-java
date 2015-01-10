package extendstest;

import org.sahagin.runlib.external.TestDoc;

public abstract class ExtendsPageBase {

    public void overriddenMethod(String arg) {}

    public abstract void abstractMethod(int arg);

    @TestDoc("Doc: finalMethod: {arg}")
    public final void finalMethod(Object arg) {}

}
