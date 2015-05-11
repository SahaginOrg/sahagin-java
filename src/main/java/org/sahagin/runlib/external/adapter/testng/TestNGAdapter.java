package org.sahagin.runlib.external.adapter.testng;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.sahagin.runlib.external.adapter.Adapter;
import org.sahagin.runlib.external.adapter.AdapterContainer;
import org.sahagin.runlib.external.adapter.ResourceAdditionalTestDocsAdapter;
import org.sahagin.runlib.external.adapter.JavaRootMethodAdapter;
import org.sahagin.runlib.srctreegen.ASTUtils;
import org.sahagin.share.CommonPath;

public class TestNGAdapter implements Adapter {

    @Override
    public void initialSetAdapter() {
        AdapterContainer container = AdapterContainer.globalInstance();
        container.setRootMethodAdapter(new RootMethodAdapterImpl(getName()));
        container.addAdditionalTestDocsAdapter(new AdditionalTestDocsAdapterImpl());
    }

    @Override
    public String getName() {
        return "testNG";
    }

    private static class RootMethodAdapterImpl implements JavaRootMethodAdapter {
        private String name;

        private RootMethodAdapterImpl(String name) {
            this.name = name;
        }

        @Override
        public boolean isRootMethod(IMethodBinding methodBinding) {
            // TODO maybe should check if public and void return method
            return ASTUtils.getAnnotationBinding(
                    methodBinding.getAnnotations(), "org.testng.annotations.Test") != null;
        }

        @Override
        public String getName() {
            return name;
        }

    }

    private static class AdditionalTestDocsAdapterImpl extends ResourceAdditionalTestDocsAdapter {

        @Override
        public String resourceDirPath() {
            return CommonPath.standardAdapdaterLocaleResDirPath("java") + "/testng";
        }

        @Override
        public void classAdd() {
        }

        @Override
        public void methodAdd() {
            // in alphabetical order
            methodAdd("org.hamcrest.MatcherAssert", "assertThat", "Object,org.hamcrest.Matcher");
            methodAdd("org.hamcrest.MatcherAssert", "assertThat", "String,Object,org.hamcrest.Matcher");
            methodAdd("org.testng.Assert", "assertEquals", "boolean,boolean");
            methodAdd("org.testng.Assert", "assertEquals", "int,int");
            methodAdd("org.testng.Assert", "assertEquals", "long,long");
            methodAdd("org.testng.Assert", "assertEquals", "Object,Object");
            methodAdd("org.testng.Assert", "assertEquals", "Object[],Object[]");
            methodAdd("org.testng.Assert", "assertEquals", "String,String");
        }

    }

}
