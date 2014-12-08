package org.sahagin.java.adapter;

import javassist.CtMethod;

import org.eclipse.jdt.core.dom.IMethodBinding;

public interface RootFunctionAdapter {

    boolean isRootFunction(IMethodBinding methodBinding);

    boolean isRootFunction(CtMethod method);

}
