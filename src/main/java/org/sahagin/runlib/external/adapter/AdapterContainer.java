package org.sahagin.runlib.external.adapter;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.sahagin.runlib.additionaltestdoc.AdditionalTestDocs;
import org.sahagin.share.AcceptableLocales;

import javassist.CtMethod;

public class AdapterContainer {
    private static AdapterContainer globalInstance = new AdapterContainer();
    private boolean initialized = false;
    private AcceptableLocales locales;
    private RootFunctionAdapter rootFunctionAdapter;
    private ScreenCaptureAdapter screenCaptureAdapter;
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
        if (!initialized) {
        	throw new IllegalStateException("initialize not called");
        }
        additionalTestDocsAdapter.add(additionalTestDocs, locales);
    }

    public AdditionalTestDocs getAdditionalTestDocs() {
        return additionalTestDocs;
    }

}
