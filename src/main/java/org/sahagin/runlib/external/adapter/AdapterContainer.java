package org.sahagin.runlib.external.adapter;

import org.sahagin.runlib.additionaltestdoc.AdditionalTestDocs;
import org.sahagin.share.AcceptableLocales;

public abstract class AdapterContainer {
    protected static AdapterContainer globalInstance;
    private AcceptableLocales locales;
    private ScreenCaptureAdapter screenCaptureAdapter;
    private AdditionalTestDocs additionalTestDocs = new AdditionalTestDocs();

    protected AdapterContainer(AcceptableLocales locales) {
        if (locales == null) {
            throw new NullPointerException();
        }
        this.locales = locales;
        this.screenCaptureAdapter = null;
        this.additionalTestDocs = new AdditionalTestDocs();
    }

    public static AdapterContainer globalInstance() {
        if (globalInstance == null) {
            throw new IllegalStateException("globalInitialize is not called yet");
        }
        return globalInstance;
    }

    // set null if don't need screen capture
    public final void setScreenCaptureAdapter(ScreenCaptureAdapter screenCaptureAdapter) {
        this.screenCaptureAdapter = screenCaptureAdapter;
    }

    public final byte[] captureScreen() {
        if (screenCaptureAdapter == null) {
            return null;
        }
        return screenCaptureAdapter.captureScreen();
    }

    // TODO throw error if called　from other method than initialSetAdapter
    public final void addAdditionalTestDocsAdapter(
            AdditionalTestDocsAdapter additionalTestDocsAdapter) {
        if (additionalTestDocsAdapter == null) {
            throw new NullPointerException();
        }
        additionalTestDocsAdapter.add(additionalTestDocs, locales);
    }

    public final AdditionalTestDocs getAdditionalTestDocs() {
        return additionalTestDocs;
    }

}
