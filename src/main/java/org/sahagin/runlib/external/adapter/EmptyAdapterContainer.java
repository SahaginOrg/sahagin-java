package org.sahagin.runlib.external.adapter;

import org.sahagin.share.AcceptableLocales;

// the simplest AdapterContainer.
// It is just for default value.
public class EmptyAdapterContainer extends AdapterContainer {

    private EmptyAdapterContainer(AcceptableLocales locales) {
        super(locales);
    }

    public EmptyAdapterContainer() {
        this(AcceptableLocales.getInstance(null));
    }
}
