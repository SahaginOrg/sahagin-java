package org.sahagin.runlib.runresultsgen;

import java.util.ArrayList;
import java.util.List;

import org.sahagin.share.runresults.StackLine;
import org.sahagin.share.srctree.SrcTree;
import org.sahagin.share.srctree.TestFuncTable;
import org.sahagin.share.srctree.TestFunction;
import org.sahagin.share.srctree.code.CodeLine;

class StackLineUtils {

    // returns true if some codeLine of function locates
    // between funcDeclareStartLine and funcDeclareEndLine
    private static boolean funcLineMatches(TestFunction func,
            int funcDeclareStartLine, int funcDeclareEndLine) {
        for (CodeLine codeLine : func.getCodeBody()) {
            if (funcDeclareStartLine <= codeLine.getStartLine()
                    && codeLine.getEndLine() <= funcDeclareEndLine) {
                return true;
            }
        }
        return false;
    }

    // returns the TestFunction whose name matches to funcQualifiedName
    // and codeBody locates between funcDeclareStartLine and funcDeclareEndLine
    private static TestFunction getTestFunction(TestFuncTable table,
            String funcQualifiedName, int funcDeclareStartLine, int funcDeclareEndLine) {
        List<TestFunction> nameFuncs = table.getByQualifiedName(funcQualifiedName);
        for (TestFunction func : nameFuncs) {
            if (funcLineMatches(func, funcDeclareStartLine, funcDeclareEndLine)) {
                return func;
            }
        }
        return null;
    }

    // returns the TestFunction whose name matches to funcQualifiedName
    // and codeBody locates between funcDeclareStartLine and funcDeclareEndLine
    public static TestFunction getTestFunction(SrcTree srcTree,
            String funcQualifiedName, int funcDeclareStartLine, int funcDeclareEndLine) {
        TestFunction rootFunction = getTestFunction(
                srcTree.getRootFuncTable(), funcQualifiedName, funcDeclareStartLine, funcDeclareEndLine);
        if (rootFunction != null) {
            return rootFunction;
        }
        TestFunction subFunction = getTestFunction(
                srcTree.getSubFuncTable(), funcQualifiedName, funcDeclareStartLine, funcDeclareEndLine);
        if (subFunction != null) {
            return subFunction;
        }
        return null;
    }

    // - assume only 1 root function line exists in currentStackTrace
    //   and overloaded root method does not exists
    // returns null if not found
    public static TestFunction getRootFunction(
            TestFuncTable rootFuncTable, StackTraceElement[] currentStackTrace) {
        for (StackTraceElement element : currentStackTrace) {
            List<TestFunction> rootFunctions
            = rootFuncTable.getByQualifiedName(element.getClassName() + "." + element.getMethodName());
            if (rootFunctions.size() > 0) {
                assert rootFunctions.size() == 1;
                return rootFunctions.get(0);
            }
        }
        return null;
    }

    // return null if not found
    private static StackLine getStackLine(
            TestFuncTable table, String funcQualifiedName, int line) {
        if (line <= 0) {
            return null; // 0 or negative line number never matches
        }
        List<TestFunction> nameFuncs = table.getByQualifiedName(funcQualifiedName);
        for (TestFunction func : nameFuncs) {
            for (int i = 0; i < func.getCodeBody().size(); i++) {
                CodeLine codeLine = func.getCodeBody().get(i);
                if (codeLine.getStartLine() <= line && line <= codeLine.getEndLine()) {
                    StackLine result = new StackLine();
                    result.setFunctionKey(func.getKey());
                    result.setFunction(func);
                    result.setCodeBodyIndex(i);
                    result.setLine(line);
                    return result;
                }
            }
        }
        return null;
    }

    // null means method does not exists in srcTree
    private static StackLine getStackLine(SrcTree srcTree, String funcQualifiedName, int line) {
        StackLine rootStackLine = getStackLine(srcTree.getRootFuncTable(), funcQualifiedName, line);
        if (rootStackLine != null) {
            return rootStackLine;
        }
        StackLine subStackLine = getStackLine(srcTree.getSubFuncTable(), funcQualifiedName, line);
        if (subStackLine != null) {
            return subStackLine;
        }
        return null;
    }

    private static StackLine getStackLine(SrcTree srcTree, StackTraceElement element) {
        return getStackLine(
                srcTree, element.getClassName() + "." + element.getMethodName(), element.getLineNumber());
    }

    public static List<StackLine> getStackLines(SrcTree srcTree, StackTraceElement[] elements) {
        List<StackLine> stackLines = new ArrayList<StackLine>(elements.length);
        for (StackTraceElement element : elements) {
            StackLine stackLine = getStackLine(srcTree, element);
            if (stackLine != null) {
                stackLines.add(stackLine);
            }
        }
        return stackLines;
    }

    // line for hookedMethodName, hookedLine will be replaced to originalLine
    public static List<StackLine> getStackLinesReplacingActualLine(
            SrcTree srcTree, StackTraceElement[] elements,
            String hoookedMethodName, int hookedLine, int originalLine) {
        List<StackLine> stackLines = new ArrayList<StackLine>(elements.length);
        for (StackTraceElement element : elements) {
            String methodQualifiedName = element.getClassName() + "." + element.getMethodName();
            StackLine stackLine;
            if (methodQualifiedName.equals(hoookedMethodName)
                    && element.getLineNumber() == hookedLine) {
                stackLine = getStackLine(srcTree, methodQualifiedName, originalLine);
            } else {
                stackLine = getStackLine(srcTree, element);
            }
            if (stackLine != null) {
                stackLines.add(stackLine);
            }
        }
        return stackLines;
    }

}
