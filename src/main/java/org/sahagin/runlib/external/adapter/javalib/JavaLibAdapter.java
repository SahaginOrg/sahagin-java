package org.sahagin.runlib.external.adapter.javalib;

import org.sahagin.runlib.external.adapter.Adapter;
import org.sahagin.runlib.external.adapter.AdapterContainer;

public class JavaLibAdapter implements Adapter {

    @Override
    public void initialSetAdapter() {
        AdapterContainer container = AdapterContainer.globalInstance();
        container.addAdditionalTestDocsAdapter(new JavaLibAdditionalTestDocsAdapter());
    }

    @Override
    public String getName() {
        return "javaLib";
    }

}
