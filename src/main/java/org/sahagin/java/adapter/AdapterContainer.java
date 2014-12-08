package org.sahagin.java.adapter;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.sahagin.java.additionaltestdoc.AdditionalTestDocs;

import javassist.CtMethod;

public class AdapterContainer {
    private static AdapterContainer globalInstance = new AdapterContainer();
    private RootFunctionAdapter rootFunctionAdapter;
    private ScreenCaptureAdapter screenCaptureAdapter;
    private AdditionalTestDocs additionalTestDocs = new AdditionalTestDocs();

    // make constructor private
    private AdapterContainer() { }

    public static AdapterContainer globalInstance() {
        return globalInstance;
    }

    // TODO throw error if called　from other method than initialSetAdapter
    public void setRootFunctionAdapter(RootFunctionAdapter rootFunctionAdapter) {
        if (rootFunctionAdapter == null) {
            throw new NullPointerException();
        }
        this.rootFunctionAdapter = rootFunctionAdapter;
    }

    public boolean isRootFunction(CtMethod method) {
        return rootFunctionAdapter.isRootFunction(method);
    }

    public boolean isRootFunction(IMethodBinding methodBinding) {
        return rootFunctionAdapter.isRootFunction(methodBinding);
    }

    // set null if don't want screen capture
    public void setScreenCaptureAdapter(ScreenCaptureAdapter screenCaptureAdapter) {
        this.screenCaptureAdapter = screenCaptureAdapter;
    }

    public byte[] captureScreen() {
        if (screenCaptureAdapter == null) {
            return null;
        }
        return screenCaptureAdapter.captueScreen();
    }

    // TODO throw error if called　from other method than initialSetAdapter
    public void addAdditionalTestDocsAdapter(
            AdditionalTestDocsAdapter additionalTestDocsAdapter) {
        if (additionalTestDocsAdapter == null) {
            throw new NullPointerException();
        }
        // last set data is referred first
        additionalTestDocsAdapter.classAdd(additionalTestDocs);
        additionalTestDocsAdapter.funcAdd(additionalTestDocs);

    }

    public AdditionalTestDocs getAdditionalTestDocs() {
        return additionalTestDocs;
    }

}
