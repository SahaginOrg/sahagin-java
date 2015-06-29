package org.sahagin.runlib.external.adapter.javasystem;

import org.sahagin.runlib.external.adapter.Adapter;
import org.sahagin.runlib.external.adapter.AdapterContainer;

public class JavaSystemAdapter implements Adapter {

    @Override
    public void initialSetAdapter() {
        AdapterContainer container = AdapterContainer.globalInstance();
        container.addAdditionalTestDocsAdapter(new JavaSystemAdditionalTestDocsAdapter());
    }

    @Override
    public String getName() {
        return "javaSystem";
    }

}
