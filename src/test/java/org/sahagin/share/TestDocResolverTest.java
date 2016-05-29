package org.sahagin.share;

import java.util.ArrayList;

import org.junit.Test;
import org.sahagin.TestBase;
import org.sahagin.share.srctree.TestMethod;
import org.sahagin.share.srctree.code.SubMethodInvoke;
import org.sahagin.share.srctree.code.UnknownCode;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class TestDocResolverTest extends TestBase {

    @Test
    public void ifEndCondition() throws IllegalTestScriptException {
        TestMethod method = new TestMethod();
        method.setTestDoc("{arg1}{if:arg2}A{1}A{end}{if:1}B{arg2}B{end}");
        method.addArgVariable("arg1");
        method.addArgVariable("arg2");
        method.setVariableLengthArgIndex(1);

        UnknownCode arg1 = new UnknownCode();
        arg1.setOriginal("1");

        UnknownCode arg2 = new UnknownCode();
        arg2.setOriginal("2");

        SubMethodInvoke invokeNormal = new SubMethodInvoke();
        invokeNormal.setOriginal("dummy");
        invokeNormal.setSubMethod(method);
        invokeNormal.addArg(arg1);
        invokeNormal.addArg(arg2);
        String testDocNormal = TestDocResolver.placeholderResolvedMethodTestDoc(
                invokeNormal, new ArrayList<String>(0));
        assertThat(testDocNormal, is("1A2AB2B"));

        SubMethodInvoke invokeWithoutVarLenArg = new SubMethodInvoke();
        invokeWithoutVarLenArg.setOriginal("dummy");
        invokeWithoutVarLenArg.setSubMethod(method);
        invokeWithoutVarLenArg.addArg(arg1);
        String testDocWithoutOptArg = TestDocResolver.placeholderResolvedMethodTestDoc(
                invokeWithoutVarLenArg, new ArrayList<String>(0));
        assertThat(testDocWithoutOptArg, is("1"));
    }

    @Test
    public void variableLengthArgument() throws IllegalTestScriptException {
        TestMethod method = new TestMethod();
        method.setTestDoc("{arg1}{arg2}");
        method.addArgVariable("arg1");
        method.addArgVariable("arg2");
        method.setVariableLengthArgIndex(1);

        UnknownCode arg1 = new UnknownCode();
        arg1.setOriginal("abc");

        UnknownCode arg2 = new UnknownCode();
        arg2.setOriginal("def");

        UnknownCode arg3 = new UnknownCode();
        arg3.setOriginal("ghi");

        SubMethodInvoke invokeNormal = new SubMethodInvoke();
        invokeNormal.setOriginal("dummy");
        invokeNormal.setSubMethod(method);
        invokeNormal.addArg(arg1);
        invokeNormal.addArg(arg2);
        String testDocNormal = TestDocResolver.placeholderResolvedMethodTestDoc(
                invokeNormal, new ArrayList<String>(0));
        assertThat(testDocNormal, is("abcdef"));

        SubMethodInvoke invokeWithVarLenArg = new SubMethodInvoke();
        invokeWithVarLenArg.setOriginal("dummy");
        invokeWithVarLenArg.setSubMethod(method);
        invokeWithVarLenArg.addArg(arg1);
        invokeWithVarLenArg.addArg(arg2);
        invokeWithVarLenArg.addArg(arg3);
        String testDocWithVarLength = TestDocResolver.placeholderResolvedMethodTestDoc(
                invokeWithVarLenArg, new ArrayList<String>(0));
        assertThat(testDocWithVarLength, is("abcdef, ghi"));

        SubMethodInvoke invokeWithoutVarLenArg = new SubMethodInvoke();
        invokeWithoutVarLenArg.setOriginal("dummy");
        invokeWithoutVarLenArg.setSubMethod(method);
        invokeWithoutVarLenArg.addArg(arg1);
        String testDocWithoutOptArg = TestDocResolver.placeholderResolvedMethodTestDoc(
                invokeWithoutVarLenArg, new ArrayList<String>(0));
        assertThat(testDocWithoutOptArg, is("abc"));
    }
}
