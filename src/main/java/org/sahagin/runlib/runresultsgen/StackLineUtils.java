package org.sahagin.runlib.runresultsgen;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sahagin.share.runresults.StackLine;
import org.sahagin.share.srctree.SrcTree;
import org.sahagin.share.srctree.TestMethod;
import org.sahagin.share.srctree.TestMethodTable;
import org.sahagin.share.srctree.code.CodeLine;

class StackLineUtils {

    // returns true if some codeLine of method locates
    // between methodDeclareStartLine and methodDeclareEndLine
    private static boolean methodLineMatches(TestMethod method,
            int methodDeclareStartLine, int methodDeclareEndLine) {
        for (CodeLine codeLine : method.getCodeBody()) {
            if (methodDeclareStartLine <= codeLine.getStartLine()
                    && codeLine.getEndLine() <= methodDeclareEndLine) {
                return true;
            }
        }
        return false;
    }

    // returns the TestMethod whose name matches to classQualifiedName and methodSimpleName
    // and codeBody locates between methodDeclareStartLine and methodDeclareEndLine
    private static TestMethod getTestMethod(TestMethodTable table,
            String classQualifiedName, String methodSimpleName,
            int methodDeclareStartLine, int methodDeclareEndLine) {
        List<TestMethod> nameMethods = table.getByName(classQualifiedName, methodSimpleName);
        for (TestMethod method : nameMethods) {
            if (methodLineMatches(method, methodDeclareStartLine, methodDeclareEndLine)) {
                return method;
            }
        }
        return null;
    }

    // returns the TestMethod whose name matches to classQualifiedName and methodSimpleName
    // and codeBody locates between methodDeclareStartLine and methodDeclareEndLine
    public static TestMethod getTestMethod(SrcTree srcTree,
            String classQualifiedName, String methodSimpleName,
            int methodDeclareStartLine, int methodDeclareEndLine) {
        TestMethod rootMethod = getTestMethod(srcTree.getRootMethodTable(),
                classQualifiedName, methodSimpleName, methodDeclareStartLine, methodDeclareEndLine);
        if (rootMethod != null) {
            return rootMethod;
        }
        TestMethod subMethod = getTestMethod(srcTree.getSubMethodTable(),
                classQualifiedName, methodSimpleName, methodDeclareStartLine, methodDeclareEndLine);
        if (subMethod != null) {
            return subMethod;
        }
        return null;
    }

    // - assume only 1 root method line exists in currentStackTrace
    //   and overloaded root method does not exists
    // returns null if not found
    public static TestMethod getRootMethod(
            TestMethodTable rootMethodTable, StackTraceElement[] currentStackTrace) {
        for (StackTraceElement element : currentStackTrace) {
            List<TestMethod> rootMethods
            = rootMethodTable.getByName(element.getClassName(), element.getMethodName());
            if (rootMethods.size() > 0) {
                assert rootMethods.size() == 1;
                return rootMethods.get(0);
            }
        }
        return null;
    }

    // return null if not found
    private static StackLine getStackLine(
            TestMethodTable table, String classQualifiedName, String methodSimpleName, int line) {
        if (line <= 0) {
            return null; // 0 or negative line number never matches
        }
        List<TestMethod> nameMethods = table.getByName(classQualifiedName, methodSimpleName);
        for (TestMethod method : nameMethods) {
            for (int i = 0; i < method.getCodeBody().size(); i++) {
                CodeLine codeLine = method.getCodeBody().get(i);
                if (codeLine.getStartLine() <= line && line <= codeLine.getEndLine()) {
                    StackLine result = new StackLine();
                    result.setMethodKey(method.getKey());
                    result.setMethod(method);
                    result.setCodeBodyIndex(i);
                    result.setLine(line);
                    return result;
                }
            }
        }
        return null;
    }

    // null means method does not exists in srcTree
    private static StackLine getStackLine(SrcTree srcTree,
            String classQualifiedName, String methodSimpleName, int line) {
        StackLine rootStackLine = getStackLine(
                srcTree.getRootMethodTable(), classQualifiedName, methodSimpleName, line);
        if (rootStackLine != null) {
            return rootStackLine;
        }
        StackLine subStackLine = getStackLine(
                srcTree.getSubMethodTable(), classQualifiedName, methodSimpleName, line);
        if (subStackLine != null) {
            return subStackLine;
        }
        return null;
    }

    private static StackLine getStackLine(SrcTree srcTree, StackTraceElement element) {
        return getStackLine(
                srcTree, element.getClassName(), element.getMethodName(), element.getLineNumber());
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
            String hookedClassQualifiedName, String hookedMethodSimpleName,
            int hookedLine, int originalLine) {
        List<StackLine> stackLines = new ArrayList<StackLine>(elements.length);
        for (StackTraceElement element : elements) {
            StackLine stackLine;
            if (StringUtils.equals(element.getClassName(), hookedClassQualifiedName)
                    && StringUtils.equals(element.getMethodName(), hookedMethodSimpleName)
                    && element.getLineNumber() == hookedLine) {
                stackLine = getStackLine(srcTree,
                        hookedClassQualifiedName, hookedMethodSimpleName, originalLine);
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
