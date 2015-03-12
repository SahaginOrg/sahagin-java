package org.sahagin.runlib.external.adapter.junit3;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.sahagin.runlib.external.adapter.Adapter;
import org.sahagin.runlib.external.adapter.AdapterContainer;
import org.sahagin.runlib.external.adapter.ResourceAdditionalTestDocsAdapter;
import org.sahagin.runlib.external.adapter.RootMethodAdapter;
import org.sahagin.share.CommonPath;

public class JUnit3Adapter implements Adapter {

    @Override
    public void initialSetAdapter() {
        AdapterContainer container = AdapterContainer.globalInstance();
        container.setRootMethodAdapter(new RootMethodAdapterImpl());
        container.addAdditionalTestDocsAdapter(new AdditionalTestDocsAdapterImpl());
    }

    private static class RootMethodAdapterImpl implements RootMethodAdapter {

        @Override
        public boolean isRootMethod(IMethodBinding methodBinding) {
            // TODO should check if public and no argument and void return method
            String methodName = methodBinding.getName();
            if (methodName == null || !methodName.startsWith("test")) {
                return false;
            }

            ITypeBinding defClass = methodBinding.getDeclaringClass();
            while (defClass != null) {
                String className = defClass.getQualifiedName();
                if ("junit.framework.TestCase".equals(className)) {
                    return true;
                }
                defClass = defClass.getSuperclass();
            }
            return false;
        }

    }

    private static class AdditionalTestDocsAdapterImpl extends ResourceAdditionalTestDocsAdapter {

        @Override
        public String resourceDirPath() {
            return CommonPath.standardAdapdaterLocaleResDirPath() + "/junit3";
        }

        @Override
        public void classAdd() {
        }

        @Override
        public void methodAdd() {
            // TODO if you use hamcrest with JUnit3

            // in alphabetical order
            methodAdd("junit.framework.Assert", "assertEquals", "double,double");
            methodAdd("junit.framework.Assert", "assertEquals", "long,long");
            methodAdd("junit.framework.Assert", "assertEquals", "Object,Object");
            methodAdd("junit.framework.Assert", "assertEquals", "Object[],Object[]");
        }

    }

}
