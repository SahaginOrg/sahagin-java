package org.sahagin.share;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sahagin.runlib.additionaltestdoc.AdditionalMethodTestDoc;
import org.sahagin.share.srctree.PageClass;
import org.sahagin.share.srctree.TestMethod;
import org.sahagin.share.srctree.code.Code;
import org.sahagin.share.srctree.code.MethodArgument;
import org.sahagin.share.srctree.code.StringCode;
import org.sahagin.share.srctree.code.SubMethodInvoke;
import org.sahagin.share.srctree.code.UnknownCode;

// TODO convert {method} to method name (this specification is from Allure framework)
// TODO cannot get argVariables for the files out of Config.testDir
// TODO throw error if placeholders are used in the class TestDoc
// TODO throw IllegalDataStructureException if MethodArgument argIndex is out of bounds

public class TestDocResolver {
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{[^\\{\\}]+\\}");
    private static final String MSG_INVALID_PLACEHOLDER
    = "TestDoc of \"%s\" contains invalid keyword \"%s\"";

    // returns invalid placeholder keyword in TestMethod if found.
    // returns null if not found
    public static String searchInvalidPlaceholder(TestMethod method) {
        if (method == null) {
            throw new NullPointerException();
        }
        if (method.getTestDoc() == null) {
            return null; // no TestDoc
        }
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
                } else if (!method.getArgVariables().contains(matched)) {
                    return matched;
                }
            }
        }
        return null;
    }

    // returns original if TestDoc value not found
    private static String methodTestDocSub(Code code, List<String> placeholderResolvedParentMethodArgTestDocs)
            throws IllegalTestScriptException {
        if (code instanceof StringCode) {
            return ((StringCode) code).getValue();
        } else if (code instanceof MethodArgument) {
            MethodArgument methodArg = (MethodArgument) code;
            return placeholderResolvedParentMethodArgTestDocs.get(methodArg.getArgIndex());
        } else if (code instanceof SubMethodInvoke) {
            SubMethodInvoke methodInvoke = (SubMethodInvoke) code;
            TestMethod method = methodInvoke.getSubMethod();
            assert method != null : "null for " + methodInvoke.getOriginal();
            if (method.getTestDoc() == null) {
                return methodInvoke.getOriginal();
            }
            Matcher matcher = PLACEHOLDER.matcher(method.getTestDoc());

            // replace all placeholders by Matcher
            StringBuffer buf = new StringBuffer(method.getTestDoc().length());
            while (matcher.find()) {
                String variable = matcher.group();
                variable = variable.substring(1, variable.length() - 1) ; // trim head and tail braces
                int varIndex = -1;
                boolean isIndexPattern = false;

                try {
                    varIndex = Integer.parseInt(variable);
                    isIndexPattern = true;
                } catch (NumberFormatException e) {
                    // not index pattern
                }

                List<Code> variableCodes = new ArrayList<Code>(4);
                if (!isIndexPattern && variable.equals("this")) {
                    Code variableCode = methodInvoke.getThisInstance();
                    if (variableCode == null) {
                        // When called inside the class on which this method is defined,
                        // set the class name for {this} keyword
                        variableCode = new UnknownCode();
                        variableCode.setOriginal(methodInvoke.getSubMethod().getTestClass().getSimpleName());
                    }
                    variableCodes.add(variableCode);
                } else {
                    if (!isIndexPattern) {
                        varIndex = method.getArgVariables().indexOf(variable);
                    }
                    if (varIndex < 0
                            ||
                            // TestMethod for AdditionalMethodTestDoc does not have argument information
                            // currently. TODO should have argument information and
                            // should not use isAdditionalMethodKey method
                            (varIndex >= method.getArgVariables().size()
                            && !AdditionalMethodTestDoc.isAdditionalMethodKey(method.getKey()))) {
                        throw new IllegalTestScriptException(String.format(
                                MSG_INVALID_PLACEHOLDER, method.getQualifiedName(), variable));
                    }

                    if (!method.hasVariableLengthArg()) {
                        if (varIndex >= methodInvoke.getArgs().size()) {
                            // maybe AdditionalMethodTestDoc
                            throw new IllegalTestScriptException(String.format(
                                    MSG_INVALID_PLACEHOLDER, method.getQualifiedName(), variable));
                        }
                        variableCodes.add(methodInvoke.getArgs().get(varIndex));
                    } else if (varIndex == method.getVariableLengthArgIndex()) {
                        // variable length argument.
                        // - TestDoc for variable length argument
                        //   is the comma connected string of all rest arguments.
                        // - TestDoc for variable length argument
                        //   is empty string if no rest arguments exist.
                        for (int i = varIndex; i < methodInvoke.getArgs().size(); i++) {
                            variableCodes.add(methodInvoke.getArgs().get(i));
                        }
                    } else if (varIndex > method.getVariableLengthArgIndex()) {
                            throw new IllegalTestScriptException(String.format(
                                    MSG_INVALID_PLACEHOLDER, method.getQualifiedName(), variable));
                    } else {
                        try {
                            variableCodes.add(methodInvoke.getArgs().get(varIndex));
                        } catch (IndexOutOfBoundsException e) {
                            throw new RuntimeException(method.getQualifiedName(),e);
                        }
                    }
                }

                String testDoc = "";
                for (int i = 0; i < variableCodes.size(); i++) {
                    if (i != 0) {
                        testDoc = testDoc + ", ";
                    }
                    testDoc = testDoc + methodTestDocSub(
                            variableCodes.get(i), placeholderResolvedParentMethodArgTestDocs);
                }
                matcher.appendReplacement(buf, testDoc);
            }
            matcher.appendTail(buf);
            return buf.toString();
        } else {
            return code.getOriginal();
        }
    }

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

    // returns null if Page not found
    private static String methodInvokePageTestDocNoRecursive(SubMethodInvoke methodInvoke) {
        if (!(methodInvoke.getSubMethod().getTestClass() instanceof PageClass)) {
            return null;
        }
        PageClass page = (PageClass) methodInvoke.getSubMethod().getTestClass();
        return page.getTestDoc();
    }

    // returns first found page testDoc.
    // returns null if page testDoc not found
    private static String methodInvokePageTestDocRecursive(SubMethodInvoke methodInvoke) {
        String pageTestDoc = methodInvokePageTestDocNoRecursive(methodInvoke);
        if (pageTestDoc != null) {
            return pageTestDoc;
        }
        for (Code code : methodInvoke.getArgs()) {
            if (code instanceof SubMethodInvoke) {
                String codeLinePageTestDoc
                = methodInvokePageTestDocRecursive((SubMethodInvoke) code);
                if (codeLinePageTestDoc != null) {
                    return codeLinePageTestDoc;
                }
            }
        }
        return null;
    }

    // uses first found page testDoc
    // (in most case, this is top level method related Page)
    public static String pageTestDoc(Code code) {
        if (!(code instanceof SubMethodInvoke)) {
            return null;
        }
        // TODO if invoke code is A.B.C(..), page documents in A or B are not checked
        SubMethodInvoke invoke = (SubMethodInvoke) code;
        return methodInvokePageTestDocRecursive(invoke);
    }

}
