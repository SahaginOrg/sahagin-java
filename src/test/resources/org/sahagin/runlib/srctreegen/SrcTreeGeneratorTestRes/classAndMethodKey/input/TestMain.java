package org.sahagin.runlib.srctreegen.SrcTreeGeneratorTestRes.classAndMethodKey.input;

import java.util.Date;
import java.util.List;

import org.sahagin.runlib.external.TestDoc;

public class TestMain {

    @TestDoc("Doc: simpleArg")
    public void simpleArg(int iArg, boolean bArg, String sArg, Object oArg,
            Date dArg, Integer iObjArg) {}

    @TestDoc("Doc: arrayArg")
    public void arrayArg(int[] iArg, boolean[] bArg, String[] sArg, Object[] oArg,
            Date[] dArg, Integer[] iObjArg, int[][] iArgMatrix, String[][] sArgMatrix, Date[][] dArgMatrix) {}

    @TestDoc("Doc: listArg")
    public void listArg(List<Integer> iArg, List<String> sArg, List<Object> oArg) {}

    @TestDoc("Doc: genericsArg")
    public <T, U extends Date> void genericsArg(T t, U u) {}

    @TestDoc("Doc: innerClassArg")
    public <T extends InnerClass> void innerClassArg(InnerClass innerClass, InnerClass[] innerClassArray,
            InnerClass[][] innerClassMatrix,
            List<InnerClass> innerClassList, T innerClassGenerics) {}


    public class InnerClass {

        @TestDoc("Doc: innerClassMethod")
        public void innerClassMethod() {}

    }

    public static class InneStaticClass {

        @TestDoc("Doc: innerStaticClassMethod")
        public void innerStaticClassMethod() {}
    }
}
