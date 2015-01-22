package org.sahagin.runlib.external.adapter;

import javassist.CtMethod;

import org.eclipse.jdt.core.dom.IMethodBinding;

public interface RootMethodAdapter {

    boolean isRootMethod(IMethodBinding methodBinding);

    boolean isRootMethod(CtMethod method);

}
