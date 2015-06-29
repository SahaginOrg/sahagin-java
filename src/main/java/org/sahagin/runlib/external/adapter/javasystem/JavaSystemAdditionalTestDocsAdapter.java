package org.sahagin.runlib.external.adapter.javasystem;

import org.sahagin.runlib.external.CaptureStyle;
import org.sahagin.runlib.external.adapter.ResourceAdditionalTestDocsAdapter;
import org.sahagin.share.CommonPath;

public class JavaSystemAdditionalTestDocsAdapter extends ResourceAdditionalTestDocsAdapter {
    public static final String CLASS_QUALIFIED_NAME = "javaSystem";
    public static final String METHOD_ASSERT = "assert";
    public static final String METHOD_EQUALS = "equals";
    public static final String METHOD_NOT_EQUALS = "notEquals";

    @Override
    public String resourceDirPath() {
        return CommonPath.standardAdapdaterLocaleResDirPath("java") + "/javasystem";
    }

    @Override
    public void classAdd() {
    }

    @Override
    public void methodAdd() {
        // in alphabetical order
        methodAdd(CLASS_QUALIFIED_NAME, METHOD_ASSERT, null, CaptureStyle.NONE);
        methodAdd(CLASS_QUALIFIED_NAME, METHOD_EQUALS, null, CaptureStyle.NONE);
        methodAdd(CLASS_QUALIFIED_NAME, METHOD_NOT_EQUALS, null, CaptureStyle.NONE);
    }

}