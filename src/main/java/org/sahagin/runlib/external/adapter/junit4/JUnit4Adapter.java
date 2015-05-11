package org.sahagin.runlib.external.adapter.junit4;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.sahagin.runlib.external.adapter.Adapter;
import org.sahagin.runlib.external.adapter.JavaAdapterContainer;
import org.sahagin.runlib.external.adapter.JavaRootMethodAdapter;
import org.sahagin.runlib.srctreegen.ASTUtils;

public class JUnit4Adapter implements Adapter {

    @Override
    public void initialSetAdapter() {
        JavaAdapterContainer container = JavaAdapterContainer.globalInstance();
        container.setRootMethodAdapter(new JavaRootMethodAdapterImpl(getName()));
        container.addAdditionalTestDocsAdapter(new JUnit4AdditionalTestDocsAdapter());
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

}
