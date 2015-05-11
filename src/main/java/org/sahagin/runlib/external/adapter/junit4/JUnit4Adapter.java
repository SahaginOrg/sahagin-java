package org.sahagin.runlib.external.adapter.junit4;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.sahagin.runlib.external.CaptureStyle;
import org.sahagin.runlib.external.adapter.Adapter;
import org.sahagin.runlib.external.adapter.JavaAdapterContainer;
import org.sahagin.runlib.external.adapter.ResourceAdditionalTestDocsAdapter;
import org.sahagin.runlib.external.adapter.JavaRootMethodAdapter;
import org.sahagin.runlib.srctreegen.ASTUtils;
import org.sahagin.share.CommonPath;

public class JUnit4Adapter implements Adapter {

    @Override
    public void initialSetAdapter() {
        JavaAdapterContainer container = JavaAdapterContainer.globalInstance();
        container.setRootMethodAdapter(new JavaRootMethodAdapterImpl(getName()));
        container.addAdditionalTestDocsAdapter(new AdditionalTestDocsAdapterImpl());
    }

    @Override
    public String getName() {
        return "jUnit4";
    }

    private static class JavaRootMethodAdapterImpl implements JavaRootMethodAdapter {
        private String name;

        private JavaRootMethodAdapterImpl(String name) {
            this.name = name;
        }

        @Override
        public boolean isRootMethod(IMethodBinding methodBinding) {
            // TODO should check if public and no argument and void return method
            return ASTUtils.getAnnotationBinding(
                    methodBinding.getAnnotations(), "org.junit.Test") != null;
        }

        @Override
        public String getName() {
            return name;
        }

    }

    private static class AdditionalTestDocsAdapterImpl extends ResourceAdditionalTestDocsAdapter {

        @Override
        public String resourceDirPath() {
            return CommonPath.standardAdapdaterLocaleResDirPath("java") + "/junit4";
        }

        @Override
        public void classAdd() {
        }

        @Override
        public void methodAdd() {
            // in alphabetical order
            methodAdd("org.hamcrest.core.Is", "is", "Object", CaptureStyle.NONE);
            methodAdd("org.hamcrest.core.Is", "is", "org.hamcrest.Matcher", CaptureStyle.NONE);
            methodAdd("org.hamcrest.core.IsNot", "not", "Object", CaptureStyle.NONE);
            methodAdd("org.hamcrest.core.IsNot", "not", "org.hamcrest.Matcher", CaptureStyle.NONE);
            methodAdd("org.hamcrest.CoreMatchers", "is", "Object", CaptureStyle.NONE);
            methodAdd("org.hamcrest.CoreMatchers", "is", "org.hamcrest.Matcher", CaptureStyle.NONE);
            methodAdd("org.hamcrest.CoreMatchers", "not", "Object", CaptureStyle.NONE);
            methodAdd("org.hamcrest.CoreMatchers", "not", "org.hamcrest.Matcher", CaptureStyle.NONE);
            methodAdd("org.junit.Assert", "assertEquals", "double,double");
            methodAdd("org.junit.Assert", "assertEquals", "long,long");
            methodAdd("org.junit.Assert", "assertEquals", "Object,Object");
            methodAdd("org.junit.Assert", "assertEquals", "Object[],Object[]");
            methodAdd("org.junit.Assert", "assertThat", "Object,org.hamcrest.Matcher");
            methodAdd("org.junit.Assert", "assertThat", "String,Object,org.hamcrest.Matcher");
        }

    }

}
