package org.sahagin.runlib.external.adapter.junit4;

import javassist.CtMethod;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.junit.Test;
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
                    methodBinding.getAnnotations(), Test.class) != null;
        }

        @Override
        public boolean isRootMethod(CtMethod method) {
            try {
                return method.getAnnotation(Test.class) != null;
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
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
            methodAdd("org.hamcrest.core.IsNot", "not");
            methodAdd("org.hamcrest.CoreMatchers", "is", "Object");
            methodAdd("org.hamcrest.CoreMatchers", "is", "org.hamcrest.Matcher");
            methodAdd("org.hamcrest.CoreMatchers", "not");
            methodAdd("org.junit.Assert", "assertEquals");
            methodAdd("org.junit.Assert", "assertThat");
        }

    }

}
