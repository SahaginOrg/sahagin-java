package org.sahagin.share.srctree.code;

import org.sahagin.share.srctree.TestFunction;
import org.sahagin.share.srctree.TestMethod;

public class SubMethodInvoke extends SubFunctionInvoke {
    public static final String TYPE = "method";

    public String getSubMethodKey() {
        return getSubFunctionKey();
    }

    public void setSubMethodKey(String subMethodKey) {
        setSubFunctionKey(subMethodKey);
    }

    public TestMethod getSubMethod() {
        return (TestMethod)getSubFunction();
    }

    @Override
    public void setSubFunction(TestFunction subFunction) {
        if (!(subFunction instanceof TestMethod)) {
            throw new IllegalArgumentException("not testMethod: " + subFunction);
        }
        super.setSubFunction(subFunction);
    }

    public void setSubMethod(TestMethod subMethod) {
        setSubFunction(subMethod);
    }

    @Override
    protected String getType() {
        return TYPE;
    }

    @Override
    protected String getFunctionKeyName() {
        return "methodKey";
    }

}
