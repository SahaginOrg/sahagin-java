package org.sahagin.runlib.external.adapter;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.sahagin.runlib.additionaltestdoc.AdditionalTestDocs;
import org.sahagin.share.AcceptableLocales;

public class AdapterContainer {
    private static AdapterContainer globalInstance = new AdapterContainer();
    private boolean initialized = false;
    private AcceptableLocales locales;
    private String acceptableTestFramework = null;
    private JavaRootMethodAdapter rootMethodAdapter;
    private ScreenCaptureAdapter screenCaptureAdapter;
    private AdditionalTestDocs additionalTestDocs = new AdditionalTestDocs();

    // make constructor private
    private AdapterContainer() {}

    private void initialize(AcceptableLocales locales, String acceptableTestFramework) {
        if (locales == null) {
            throw new NullPointerException();
        }
        if (acceptableTestFramework == null) {
            throw new NullPointerException();
        }
        this.locales = locales;
        this.acceptableTestFramework = acceptableTestFramework;
        this.rootMethodAdapter = null;
        this.screenCaptureAdapter = null;
        this.additionalTestDocs = new AdditionalTestDocs();
        initialized = true;
    }

    // some method call of this class requires initialization before calling the method
    public static void globalInitialize(AcceptableLocales locales, String acceptableTestFramework) {
        globalInstance.initialize(locales, acceptableTestFramework);
    }

    public static AdapterContainer globalInstance() {
        if (globalInstance == null) {
            throw new IllegalStateException("globalInitialize is not called yet");
        }
        return globalInstance;
    }

    // TODO throw error if called　from other method than initialSetAdapter
    public void setRootMethodAdapter(JavaRootMethodAdapter rootMethodAdapter) {
        if (rootMethodAdapter == null) {
            throw new NullPointerException();
        }
        if (acceptableTestFramework == null) {
            throw new IllegalStateException("acceptableTestFramework is not set");
        }
        if (acceptableTestFramework.equals(rootMethodAdapter.getName())) {
            this.rootMethodAdapter = rootMethodAdapter;
        }
    }

    public boolean isRootMethodAdapterSet() {
        return this.rootMethodAdapter != null;
    }

    public boolean isRootMethod(IMethodBinding methodBinding) {
        return rootMethodAdapter.isRootMethod(methodBinding);
    }

    // set null if don't need screen capture
    public void setScreenCaptureAdapter(ScreenCaptureAdapter screenCaptureAdapter) {
        this.screenCaptureAdapter = screenCaptureAdapter;
    }

    public byte[] captureScreen() {
        if (screenCaptureAdapter == null) {
            return null;
        }
        return screenCaptureAdapter.captureScreen();
    }

    // TODO throw error if called　from other method than initialSetAdapter
    public void addAdditionalTestDocsAdapter(
            AdditionalTestDocsAdapter additionalTestDocsAdapter) {
        if (additionalTestDocsAdapter == null) {
            throw new NullPointerException();
        }
        if (!initialized) {
            throw new IllegalStateException("initialize not called");
        }
        additionalTestDocsAdapter.add(additionalTestDocs, locales);
    }

    public AdditionalTestDocs getAdditionalTestDocs() {
        return additionalTestDocs;
    }

}
