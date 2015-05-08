package org.sahagin.share;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.sahagin.share.srctree.PageClass;
import org.sahagin.share.srctree.TestClass;
import org.sahagin.share.srctree.TestMethod;
import org.sahagin.share.srctree.code.Code;
import org.sahagin.share.srctree.code.Field;
import org.sahagin.share.srctree.code.LocalVar;
import org.sahagin.share.srctree.code.MethodArgument;
import org.sahagin.share.srctree.code.StringCode;
import org.sahagin.share.srctree.code.SubMethodInvoke;
import org.sahagin.share.srctree.code.TestStep;
import org.sahagin.share.srctree.code.TestStepLabel;
import org.sahagin.share.srctree.code.UnknownCode;
import org.sahagin.share.srctree.code.VarAssign;

// TODO convert {method} to method name (this specification is from Allure framework)
// TODO cannot get argVariables for the files out of Config.testDir
// TODO throw error if placeholders are used in the class TestDoc
// TODO throw IllegalDataStructureException if MethodArgument argIndex is out of bounds

public class TestDocResolver {
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{[^\\{\\}]+\\}");
    private static final String MSG_INVALID_PLACEHOLDER
    = "TestDoc of \"%s\" contains invalid keyword {%s}";
    private static final String MSG_INVALID_PLACEHOLDER_POS
    = "TestDoc of \"%s\" contains invalid position keyword {%s}";
    private static final String MSG_STATEMENT_NOT_CLOSED
    = "{end} keyword not found in TestDoc of \"%s\"";

    // returns invalid placeholder keyword in TestMethod if found.
    // returns null if not found
    // TODO check mismatched if,else,elseif,end keywords
    public static String searchInvalidPlaceholder(TestMethod method) {
        if (method == null) {
            throw new NullPointerException();
        }
        if (method.getTestDoc() == null) {
            return null; // no TestDoc
        }
        boolean condidionalFound = false;
        Matcher matcher = PLACEHOLDER.matcher(method.getTestDoc());
        while (matcher.find()) {
            String matched = matcher.group();
            matched = matched.substring(1, matched.length() - 1) ; // trim head and tail braces
            try {
                Integer.parseInt(matched);
                // TODO index check
            } catch (NumberFormatException e) {
                // not index pattern
                if (matched.equals("this")) {
                    continue;
                } else if (matched.startsWith("if:")) {
                    condidionalFound = true;
                    continue;
                } else if (condidionalFound && matched.equals("end")) {
                    continue;
                } else if (!method.getArgVariables().contains(matched)) {
                    return matched;
                }
            }
        }
        return null;
    }

    // Gets the codes for the argument of the methodInvoke.
    // second argument..whether argument for variable is actually specified.
    // {this} and variable length argument can be empty
    private static Pair<List<Code>, Boolean> methodInvokeNormalVariableCodes(
            SubMethodInvoke methodInvoke, String variable) throws IllegalTestScriptException {
        TestMethod method = methodInvoke.getSubMethod();

        if (variable.equals("this")) {
            Code variableCode = methodInvoke.getThisInstance();
            boolean empty = false;
            if (variableCode == null) {
                // When called inside the class on which this method is defined,
                // set the class name for {this} keyword
                variableCode = new UnknownCode();
                variableCode.setOriginal(methodInvoke.getSubMethod().getTestClass().getSimpleName());
                empty = true;
            }
            return Pair.of(Arrays.asList(variableCode), empty);
        }

        int varIndex = -1;
        try {
            varIndex = Integer.parseInt(variable);
        } catch (NumberFormatException e) {
            // not index pattern
            varIndex = method.getArgVariables().indexOf(variable);
        }

        if (varIndex < 0) {
            // maybe failed to calculate varIndex from variable
            throw new IllegalTestScriptException(String.format(
                    MSG_INVALID_PLACEHOLDER, method.getQualifiedName(), variable));
        }

        // TODO maybe should check that varIndex >= method.getArgVariables().size()
        // only when method actually has argument variable information

        if (!method.hasVariableLengthArg()) {
            if (varIndex >= methodInvoke.getArgs().size()) {
                // maybe AdditionalMethodTestDoc
                throw new IllegalTestScriptException(String.format(
                        MSG_INVALID_PLACEHOLDER, method.getQualifiedName(), variable));
            }
            return Pair.of(Arrays.asList(methodInvoke.getArgs().get(varIndex)), false);
        }

        if (varIndex == method.getVariableLengthArgIndex()) {
            // variable length argument.
            // - TestDoc for variable length argument
            //   is the comma connected string of all rest arguments.
            // - TestDoc for variable length argument
            //   is empty string if no rest arguments exist.
            List<Code> variableCodes = new ArrayList<Code>(4);
            for (int i = varIndex; i < methodInvoke.getArgs().size(); i++) {
                variableCodes.add(methodInvoke.getArgs().get(i));
            }
            return Pair.of(variableCodes, variableCodes.isEmpty());
        }

        if (varIndex > method.getVariableLengthArgIndex()) {
            throw new IllegalTestScriptException(String.format(
                    MSG_INVALID_PLACEHOLDER, method.getQualifiedName(), variable));
        }

        try {
            return Pair.of(Arrays.asList(methodInvoke.getArgs().get(varIndex)), false);
        } catch (IndexOutOfBoundsException e) {
            throw new RuntimeException(method.getQualifiedName(), e);
        }
    }

    private static String methodInvokeNormalVariableTestDoc(
            SubMethodInvoke methodInvoke, String variable,
            List<String> placeholderResolvedParentMethodArgTestDocs) throws IllegalTestScriptException {
        List<Code> variableCodes = methodInvokeNormalVariableCodes(methodInvoke, variable).getLeft();
        String testDoc = "";
        for (int i = 0; i < variableCodes.size(); i++) {
            if (i != 0) {
                testDoc = testDoc + ", ";
            }
            testDoc = testDoc + methodTestDocSub(
                    variableCodes.get(i), placeholderResolvedParentMethodArgTestDocs);
        }
        return testDoc;
    }

    private static boolean validateCondVariable(SubMethodInvoke methodInvoke, String condVariable,
            List<String> placeholderResolvedParentMethodArgTestDocs) throws IllegalTestScriptException {
        String[] splitted = condVariable.split(":");
        if (splitted.length != 2) {
            throw new IllegalTestScriptException(String.format(
                    MSG_INVALID_PLACEHOLDER, methodInvoke.getSubMethod().getQualifiedName(), condVariable));
        }
        String expressionVariable = splitted[1];
        // returns true if argument for expressionVariable is not empty
        return !methodInvokeNormalVariableCodes(methodInvoke, expressionVariable).getRight();
    }

    private static String methodInvokeTestDoc(SubMethodInvoke methodInvoke,
            List<String> placeholderResolvedParentMethodArgTestDocs) throws IllegalTestScriptException {
        TestMethod method = methodInvoke.getSubMethod();
        assert method != null : "null for " + methodInvoke.getOriginal();
        if (method.getTestDoc() == null) {
            return methodInvoke.getOriginal();
        }
        Matcher matcher = PLACEHOLDER.matcher(method.getTestDoc());

        // replace all placeholders by Matcher
        StringBuffer buf = new StringBuffer(method.getTestDoc().length());
        StringBuffer dummy = new StringBuffer(method.getTestDoc().length());
        boolean ifFound = false;
        boolean insideIf = false;
        boolean skip = false;
        // TODO support nested condition, and else, elseif condition
        while (matcher.find()) {
            String variable = matcher.group();
            variable = variable.substring(1, variable.length() - 1) ; // trim head and tail braces
            if (variable.startsWith("if:")) {
                if (insideIf) {
                    // nested if is not supported yet
                    throw new IllegalTestScriptException(String.format(
                            MSG_INVALID_PLACEHOLDER_POS, method.getQualifiedName(), variable));
                }
                matcher.appendReplacement(buf, "");
                ifFound = true;
                insideIf = true;
                skip = !validateCondVariable(methodInvoke, variable, placeholderResolvedParentMethodArgTestDocs);
            } else if (variable.equals("end") && ifFound) {
                if (!insideIf) {
                    throw new IllegalTestScriptException(String.format(
                            MSG_INVALID_PLACEHOLDER_POS, method.getQualifiedName(), variable));
                }
                if (skip) {
                    matcher.appendReplacement(dummy, ""); // abort data
                } else {
                    matcher.appendReplacement(buf, "");
                }
                insideIf = false;
                skip = false;
            } else if (skip) {
                matcher.appendReplacement(dummy, ""); // abort data
            } else {
                String testDoc = methodInvokeNormalVariableTestDoc(
                        methodInvoke, variable, placeholderResolvedParentMethodArgTestDocs);
                // escape $ to avoid appendReplacement method error
                testDoc = testDoc.replace("$", "\\$");
                try {
                    matcher.appendReplacement(buf, testDoc);
                } catch (RuntimeException e) {
                    throw new RuntimeException("fail to append: " + testDoc, e);
                }
            }
        }
        if (insideIf) {
            throw new IllegalTestScriptException(String.format(
                    MSG_STATEMENT_NOT_CLOSED, method.getQualifiedName()));
        }
        matcher.appendTail(buf);
        return buf.toString();
    }

    // returns original if TestDoc value not found
    private static String methodTestDocSub(Code code,
            List<String> placeholderResolvedParentMethodArgTestDocs)
            throws IllegalTestScriptException {
        if (code instanceof StringCode) {
            return ((StringCode) code).getValue();
        } else if (code instanceof MethodArgument) {
            MethodArgument methodArg = (MethodArgument) code;
            return placeholderResolvedParentMethodArgTestDocs.get(methodArg.getArgIndex());
        } else if (code instanceof SubMethodInvoke) {
            SubMethodInvoke methodInvoke = (SubMethodInvoke) code;
            return methodInvokeTestDoc(methodInvoke, placeholderResolvedParentMethodArgTestDocs);
        } else if (code instanceof LocalVar) {
            return String.format(SysMessages.get(SysMessages.LOCAL_VAR),
                    ((LocalVar) code).getName());
        } else if (code instanceof Field) {
            String testDoc = ((Field) code).getField().getTestDoc();
            if (testDoc == null) {
                // TODO resolve {this} keyword in the testDoc of the Field
                return code.getOriginal();
            } else {
                return testDoc;
            }
        } else if (code instanceof VarAssign) {
            VarAssign assign = (VarAssign) code;
            String variableTestDoc = methodTestDocSub(
                    assign.getVariable(), placeholderResolvedParentMethodArgTestDocs);
            String valueTestDoc = methodTestDocSub(
                    assign.getValue(), placeholderResolvedParentMethodArgTestDocs);
            return String.format(SysMessages.get(SysMessages.VAR_ASSIGN),
                    valueTestDoc, variableTestDoc);
        } else if (code instanceof TestStep) {
            TestStep testStep = (TestStep) code;
            String result = "";
            if (testStep.getLabel() != null) {
                result = testStep.getLabel() + ": ";
            }
            if (testStep.getText() != null) {
                result = result + testStep.getText();
            }
            return result;
        } else if (code instanceof TestStepLabel) {
            TestStepLabel testStepLabel = (TestStepLabel) code;
            String result = "";
            if (testStepLabel.getLabel() != null) {
                // testStep text does not contain placeholder
                result = testStepLabel.getLabel() + ": ";
            }
            if (testStepLabel.getText() != null) {
                // testStepLabel text does not contain placeholder
                result = result + testStepLabel.getText();
            }
            return result;
        } else {
            return code.getOriginal();
        }
    }

    // code: SubMethodInvoke code
    public static List<String> placeholderResolvedMethodArgTestDocs(Code code,
            List<String> placeholderResolvedParentMethodArgTestDocs) throws IllegalTestScriptException {
        if (!(code instanceof SubMethodInvoke)) {
            return new ArrayList<String>(0);
        }
        SubMethodInvoke methodInvoke = (SubMethodInvoke) code;
        List<String> result = new ArrayList<String>(methodInvoke.getArgs().size());
        for (Code arg : methodInvoke.getArgs()) {
            String argStr = methodTestDocSub(arg, placeholderResolvedParentMethodArgTestDocs);
            result.add(argStr);
        }
        return result;
    }

    public static String placeholderResolvedMethodTestDoc(
            Code code, List<String> placeholderResolvedParentMethodArgTestDocs)
            throws IllegalTestScriptException {
        if (code instanceof UnknownCode) {
            return null; // UnknownCode TestDoc is null
        } else {
            return methodTestDocSub(code, placeholderResolvedParentMethodArgTestDocs);
        }
    }

    // Returns first found page testDoc.
    // returns null if page testDoc not found
    public static PageClass codePage(Code code) {
        if (code instanceof SubMethodInvoke) {
            SubMethodInvoke invoke = (SubMethodInvoke) code;
            TestClass testClass = invoke.getSubMethod().getTestClass();
            if (testClass instanceof PageClass) {
                return (PageClass) testClass;
            }
            PageClass thisPage = codePage(invoke.getThisInstance());
            if (thisPage != null) {
                return thisPage;
            }
            for (Code arg : invoke.getArgs()) {
                PageClass argPage = codePage(arg);
                if (argPage != null) {
                    return argPage;
                }
            }
        } else if (code instanceof Field) {
            Field field = (Field) code;
            TestClass testClass = field.getField().getTestClass();
            if (testClass instanceof PageClass) {
                return (PageClass) testClass;
            }
            PageClass thisPage = codePage(field.getThisInstance());
            if (thisPage != null) {
                return thisPage;
            }
        } else if (code instanceof VarAssign) {
            VarAssign assign = (VarAssign) code;
            PageClass varPage = codePage(assign.getVariable());
            if (varPage != null) {
                return varPage;
            }
            PageClass valuePage = codePage(assign.getValue());
            if (valuePage != null) {
                return valuePage;
            }
        }
        return null;
    }

}
