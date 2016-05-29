package org.sahagin.runlib.external.adapter.javalib;

import org.sahagin.runlib.external.CaptureStyle;
import org.sahagin.runlib.external.adapter.ResourceAdditionalTestDocsAdapter;
import org.sahagin.share.CommonPath;

public class JavaLibAdditionalTestDocsAdapter extends ResourceAdditionalTestDocsAdapter {

    @Override
    public String resourceDirPath() {
        return CommonPath.standardAdapdaterLocaleResDirPath("java") + "/javalib";
    }

    @Override
    public void classAdd() {}

    @Override
    public void methodAdd() {
        // in alphabetical order
        methodAdd("java.util.List", "get", CaptureStyle.NONE);
    }
}