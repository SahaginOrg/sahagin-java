package org.sahagin.runlib.external.adapter.junit4;

import org.sahagin.runlib.external.CaptureStyle;
import org.sahagin.runlib.external.adapter.ResourceAdditionalTestDocsAdapter;
import org.sahagin.share.CommonPath;

public class JUnit4AdditionalTestDocsAdapter extends ResourceAdditionalTestDocsAdapter {

    @Override
    public String resourceDirPath() {
        return CommonPath.standardAdapdaterLocaleResDirPath("java") + "/junit4";
    }

    @Override
    public void classAdd() {}

    @Override
    public void methodAdd() {
        // in alphabetical order
        methodAdd("org.hamcrest.core.Is", "is", "Object", CaptureStyle.NONE);
        methodAdd("org.hamcrest.core.Is", "is", "org.hamcrest.Matcher", CaptureStyle.NONE);
        methodAdd("org.hamcrest.core.IsNot", "not", "Object", CaptureStyle.NONE);
        methodAdd("org.hamcrest.core.IsNot", "not", "org.hamcrest.Matcher", CaptureStyle.NONE);
        methodAdd("org.hamcrest.CoreMatchers", "is", "Object", CaptureStyle.NONE);
        methodAdd("org.hamcrest.CoreMatchers", "is", "org.hamcrest.Matcher", CaptureStyle.NONE);
        methodAdd("org.hamcrest.CoreMatchers", "not", "Object", CaptureStyle.NONE);
        methodAdd("org.hamcrest.CoreMatchers", "not", "org.hamcrest.Matcher", CaptureStyle.NONE);
        methodAdd("org.junit.Assert", "assertEquals", "double,double");
        methodAdd("org.junit.Assert", "assertEquals", "long,long");
        methodAdd("org.junit.Assert", "assertEquals", "Object,Object");
        methodAdd("org.junit.Assert", "assertEquals", "Object[],Object[]");
        methodAdd("org.junit.Assert", "assertThat", "Object,org.hamcrest.Matcher");
        methodAdd("org.junit.Assert", "assertThat", "String,Object,org.hamcrest.Matcher");
    }
}