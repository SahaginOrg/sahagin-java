package org.sahagin.runlib.external.adapter.junit4;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.sahagin.runlib.external.adapter.Adapter;
import org.sahagin.runlib.external.adapter.AdapterContainer;
import org.sahagin.runlib.external.adapter.ResourceAdditionalTestDocsAdapter;
import org.sahagin.runlib.external.adapter.RootMethodAdapter;
import org.sahagin.runlib.srctreegen.ASTUtils;
import org.sahagin.share.CommonPath;

public class JUnit4Adapter implements Adapter {

    @Override
    public void initialSetAdapter() {
        AdapterContainer container = AdapterContainer.globalInstance();
        container.setRootMethodAdapter(new RootMethodAdapterImpl());
        container.addAdditionalTestDocsAdapter(new AdditionalTestDocsAdapterImpl());
    }

    private static class RootMethodAdapterImpl implements RootMethodAdapter {

        @Override
        public boolean isRootMethod(IMethodBinding methodBinding) {
            return ASTUtils.getAnnotationBinding(
                    methodBinding.getAnnotations(), "org.junit.Test") != null;
        }

    }

    private static class AdditionalTestDocsAdapterImpl extends ResourceAdditionalTestDocsAdapter {

        @Override
        public String resourceDirPath() {
            return CommonPath.standardAdapdaterLocaleResDirPath() + "/junit4";
        }

        @Override
        public void classAdd() {
        }

        @Override
        public void methodAdd() {
            // TODO cannot handle methods defined on subclass

            // in alphabetical order
            methodAdd("org.hamcrest.core.Is", "is", "Object");
            methodAdd("org.hamcrest.core.Is", "is", "org.hamcrest.Matcher");
            methodAdd("org.hamcrest.core.IsNot", "not", "Object");
            methodAdd("org.hamcrest.core.IsNot", "not", "org.hamcrest.Matcher");
            methodAdd("org.hamcrest.CoreMatchers", "is", "Object");
            methodAdd("org.hamcrest.CoreMatchers", "is", "org.hamcrest.Matcher");
            methodAdd("org.hamcrest.CoreMatchers", "not", "Object");
            methodAdd("org.hamcrest.CoreMatchers", "not", "org.hamcrest.Matcher");
            methodAdd("org.junit.Assert", "assertEquals", "double,double");
            methodAdd("org.junit.Assert", "assertEquals", "long,long");
            methodAdd("org.junit.Assert", "assertEquals", "Object,Object");
            methodAdd("org.junit.Assert", "assertEquals", "Object[],Object[]");
            methodAdd("org.junit.Assert", "assertThat", "Object,org.hamcrest.Matcher");
            methodAdd("org.junit.Assert", "assertThat", "String,Object,org.hamcrest.Matcher");
        }

    }

}
