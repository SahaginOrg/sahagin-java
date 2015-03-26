package org.sahagin.runlib.external.adapter;

import org.eclipse.jdt.core.dom.IMethodBinding;

public interface RootMethodAdapter {

    boolean isRootMethod(IMethodBinding methodBinding);

    String getName();

}
