package org.sahagin.runlib.external.adapter.javalib;

import org.sahagin.runlib.external.CaptureStyle;
import org.sahagin.runlib.external.adapter.Adapter;
import org.sahagin.runlib.external.adapter.AdapterContainer;
import org.sahagin.runlib.external.adapter.ResourceAdditionalTestDocsAdapter;
import org.sahagin.share.CommonPath;

public class JavaLibAdapter implements Adapter {

    @Override
    public void initialSetAdapter() {
        AdapterContainer container = AdapterContainer.globalInstance();
        container.addAdditionalTestDocsAdapter(new AdditionalTestDocsAdapterImpl());
    }

    private static class AdditionalTestDocsAdapterImpl extends ResourceAdditionalTestDocsAdapter {

        @Override
        public String resourceDirPath() {
            return CommonPath.standardAdapdaterLocaleResDirPath() + "/javalib";
        }

        @Override
        public void classAdd() {
        }

        @Override
        public void methodAdd() {
            // in alphabetical order
            methodAdd("java.util.List", "get", CaptureStyle.NONE);
        }

    }

}
