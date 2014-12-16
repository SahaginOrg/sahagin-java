package org.sahagin.java.external.junit4;

import javassist.CtMethod;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.junit.Test;
import org.sahagin.java.adapter.Adapter;
import org.sahagin.java.adapter.AdapterContainer;
import org.sahagin.java.adapter.AdditionalTestDocsAdapter;
import org.sahagin.java.adapter.RootFunctionAdapter;
import org.sahagin.java.additionaltestdoc.AdditionalTestDocs;
import org.sahagin.java.srctreegen.ASTUtils;

public class JUnit4Adapter implements Adapter {

    @Override
    public void initialSetAdapter() {
        AdapterContainer container = AdapterContainer.globalInstance();
        container.setRootFunctionAdapter(new RootFunctionAdapterImpl());
        container.addAdditionalTestDocsAdapter(new AdditionalTestDocsAdapterImpl());
    }

    private static class RootFunctionAdapterImpl implements RootFunctionAdapter {

        @Override
        public boolean isRootFunction(IMethodBinding methodBinding) {
            return ASTUtils.getAnnotationBinding(methodBinding.getAnnotations(), Test.class) != null;
        }

        @Override
        public boolean isRootFunction(CtMethod method) {
            try {
                return method.getAnnotation(Test.class) != null;
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private static class AdditionalTestDocsAdapterImpl implements AdditionalTestDocsAdapter {

        @Override
        public void classAdd(AdditionalTestDocs docs) {
            docs.classAdd("org.junit.Assert", "値チェック");
        }

        @Override
        public void funcAdd(AdditionalTestDocs docs) {
            // TODO cannot handle methods defined on subclass
            // TODO multiple language support

            // alphabetical order
            docs.methodAdd("org.hamcrest.core.Is", "is", "「{0}」に等しい");
            docs.methodAdd("org.junit.Assert", "assertEquals", "「{0}」が「{1}」に等しいことをチェック");
            docs.methodAdd("org.junit.Assert", "assertThat", "「{0}」が{1}ことをチェック");
        }

    }

}
