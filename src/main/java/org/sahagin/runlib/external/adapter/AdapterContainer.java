package org.sahagin.runlib.external.adapter;

import java.util.logging.Logger;

import org.sahagin.runlib.additionaltestdoc.AdditionalTestDocs;
import org.sahagin.share.AcceptableLocales;
import org.sahagin.share.Logging;

public abstract class AdapterContainer {
    private static Logger logger = Logging.getLogger(AdapterContainer.class.getName());
    private static AdapterContainer globalInstance = new EmptyAdapterContainer();
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
        if (globalInstance instanceof EmptyAdapterContainer) {
            logger.severe("You should not use EmptyAdapterContainer. Use other concrete AdapterContainer");
            String message = String.format("Sahagin: Global instance error. Maybe javaagent option is not recognized properly.");
            // Don't throw error and just output fatal error message
            // (User may want to switch sahagin enabled by just removing javaagent JVM argument)
            // TODO should abolish javaagent argument.
            logger.severe(message);
            System.err.println(message);
        }
        return globalInstance;
    }

    protected static void setGlobalInstance(AdapterContainer instance) {
        if (instance == null) {
            throw new NullPointerException();
        }
        globalInstance = instance;
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
