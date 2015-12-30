package org.sahagin.runlib.external.adapter;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.sahagin.share.AcceptableLocales;

public class JavaAdapterContainer extends AdapterContainer {
    private String acceptableTestFramework = null;
    private JavaRootMethodAdapter rootMethodAdapter;

    // make constructor private
    private JavaAdapterContainer(AcceptableLocales locales, String acceptableTestFramework) {
        super(locales);
        if (acceptableTestFramework == null) {
            throw new NullPointerException();
        }
        this.acceptableTestFramework = acceptableTestFramework;
        this.rootMethodAdapter = null;
    }

    // some method call of this class requires initialization before calling the method
    public static void globalInitialize(AcceptableLocales locales, String acceptableTestFramework) {
        setGlobalInstance(new JavaAdapterContainer(locales, acceptableTestFramework));
    }

    public static JavaAdapterContainer globalInstance() {
        if (!(AdapterContainer.globalInstance() instanceof JavaAdapterContainer)) {
            throw new IllegalStateException(
                    "global instance is not JavaAdapterContainer: " + AdapterContainer.globalInstance());
        }
        return (JavaAdapterContainer) AdapterContainer.globalInstance();
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
}
