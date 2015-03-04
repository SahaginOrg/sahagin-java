package org.sahagin.runlib.external.adapter;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.sahagin.runlib.additionaltestdoc.AdditionalTestDocs;
import org.sahagin.share.AcceptableLocales;
import org.sahagin.share.runresults.RootMethodRunResult;

import javassist.CtMethod;

public class AdapterContainer {
    private static AdapterContainer globalInstance = new AdapterContainer();
    private boolean initialized = false;
    private AcceptableLocales locales;
    private RootMethodAdapter rootMethodAdapter;
    private ScreenCaptureAdapter screenCaptureAdapter;
    private ScreenSizeAdapter screenSizeAdapter;
    private AdditionalTestDocs additionalTestDocs = new AdditionalTestDocs();

    // make constructor private
    private AdapterContainer() {}

    private void initialize(AcceptableLocales locales) {
        if (locales == null) {
            throw new NullPointerException();
        }
        this.locales = locales;
        initialized = true;
    }

    // some method call of this class requires initialization before calling the method
    public static void globalInitialize(AcceptableLocales locales) {
        globalInstance.initialize(locales);
    }

    public static AdapterContainer globalInstance() {
        if (globalInstance == null) {
            throw new IllegalStateException("globalInitialize is not called yet");
        }
        return globalInstance;
    }

    // TODO throw error if called　from other method than initialSetAdapter
    public void setRootMethodAdapter(RootMethodAdapter rootMethodAdapter) {
        if (rootMethodAdapter == null) {
            throw new NullPointerException();
        }
        this.rootMethodAdapter = rootMethodAdapter;
    }

    public boolean isRootMethod(CtMethod method) {
        return rootMethodAdapter.isRootMethod(method);
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
        return screenCaptureAdapter.captueScreen();
    }

    // set null if don't need screen size
    public void setScreenSizeAdapter(ScreenSizeAdapter screenSizeAdapter) {
        this.screenSizeAdapter = screenSizeAdapter;
    }

    public int getScreenWidth() {
        if (screenSizeAdapter == null) {
            return RootMethodRunResult.WIDTH_NOT_ASSIGNED;
        }
        return screenSizeAdapter.getScreenWidth();
    }

    public int getScreenHeight() {
        if (screenSizeAdapter == null) {
            return RootMethodRunResult.HEIGHT_NOT_ASSIGNED;
        }
        return screenSizeAdapter.getScreenHeight();
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
