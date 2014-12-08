package org.sahagin.share;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sahagin.share.srctree.PageClass;
import org.sahagin.share.srctree.TestFunction;
import org.sahagin.share.srctree.code.Code;
import org.sahagin.share.srctree.code.StringCode;
import org.sahagin.share.srctree.code.SubFunctionInvoke;
import org.sahagin.share.srctree.code.SubMethodInvoke;
import org.sahagin.share.srctree.code.UnknownCode;

// TODO convert {method} to method name (this specification is from Allure framework)
// TODO cannot get argVariables for the files out of Config.testDir

public class TestDocResolver {
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{[^\\{\\}]+\\}");
    private static final String MSG_INVALID_PLACEHOLDER
    = "TestDoc of \"%s\" contains invalid keyword \"%s\"";

    // returns invalid placeholder keyword in TestFunction if found.
    // returns null if not found
    public static String searchInvalidPlaceholder(TestFunction func) {
        if (func == null) {
            throw new NullPointerException();
        }
        if (func.getTestDoc() == null) {
            return null; // no TestDoc
        }
        Matcher matcher = PLACEHOLDER.matcher(func.getTestDoc());
        while (matcher.find()) {
            String matched = matcher.group();
            matched = matched.substring(1, matched.length() - 1) ; // trim head and tail braces
            try {
                Integer.parseInt(matched);
                // TODO index check
            } catch (NumberFormatException e) {
                // not index pattern
                if (!func.getArgVariables().contains(matched)) {
                    return matched;
                }
            }
        }
        return null;
    }

    // returns original if TestDoc value not found
    private static String funcTestDocSub(Code code) throws IllegalTestScriptException {
        if (code instanceof StringCode) {
            return ((StringCode) code).getValue();
        } else if (code instanceof SubFunctionInvoke) {
            SubFunctionInvoke funcInvoke = (SubFunctionInvoke) code;
            TestFunction func = funcInvoke.getSubFunction();
            assert func != null : "null for " + funcInvoke.getOriginal();
            if (func.getTestDoc() == null) {
                return funcInvoke.getOriginal();
            }
            Matcher matcher = PLACEHOLDER.matcher(func.getTestDoc());

            // replace all placeholders by Matcher
            StringBuffer buf = new StringBuffer(func.getTestDoc().length());
            while (matcher.find()) {
                String variable = matcher.group();
                variable = variable.substring(1, variable.length() - 1) ; // trim head and tail braces
                int varIndex;
                try {
                    varIndex = Integer.parseInt(variable);
                } catch (NumberFormatException e) {
                    // not index pattern
                    varIndex = func.getArgVariables().indexOf(variable);
                }
                if (varIndex < 0 || varIndex >= funcInvoke.getArgs().size()) {
                    throw new IllegalTestScriptException(String.format(
                            MSG_INVALID_PLACEHOLDER, func.getQualifiedName(), variable));
                }
                matcher.appendReplacement(buf, funcTestDocSub(funcInvoke.getArgs().get(varIndex)));
            }
            matcher.appendTail(buf);
            return buf.toString();
        } else {
            return code.getOriginal();
        }
    }

    public static String placeholderResolvedFuncTestDoc(Code code)
            throws IllegalTestScriptException {
        if (code instanceof UnknownCode) {
            return null; // UnknownCode TestDoc is null
        } else {
            return funcTestDocSub(code);
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
        SubMethodInvoke invoke = (SubMethodInvoke) code;
        return methodInvokePageTestDocRecursive(invoke);
    }

}
