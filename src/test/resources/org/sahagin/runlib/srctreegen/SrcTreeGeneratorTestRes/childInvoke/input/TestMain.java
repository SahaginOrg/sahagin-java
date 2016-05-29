package org.sahagin.runlib.srctreegen.SrcTreeGeneratorTestRes.childInvoke.input;

import org.junit.Test;
import org.sahagin.runlib.external.TestDoc;

public class TestMain {

    public class ParentClass {

        @TestDoc("Doc: parentClassMethod: {arg}")
        public void parentClassMethod(String arg) {}
    }

    public interface ParentInterface {

        @TestDoc("Doc: parentInterfaceMethod: {arg}")
        public void parentInterfaceMethod(String arg);
    }

    public class ChildClass extends ParentClass implements ParentInterface {

        @Override
        public void parentClassMethod(String arg) {
            super.parentClassMethod(arg);
        }

        @Override
        public void parentInterfaceMethod(String arg) {}
    }

    @Test
    public void callMain() {
        ChildClass child = new ChildClass();
        child.parentClassMethod("abc");
        child.parentInterfaceMethod("def");
    }
}
