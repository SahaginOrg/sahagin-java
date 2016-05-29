package org.sahagin.runlib.runresultsgen;

import java.util.ArrayList;
import java.util.List;

import org.sahagin.share.runresults.StackLine;
import org.sahagin.share.srctree.SrcTree;
import org.sahagin.share.srctree.TestMethod;
import org.sahagin.share.srctree.TestMethodTable;
import org.sahagin.share.srctree.code.CodeLine;

class StackLineUtils {

    public static abstract class LineReplacer {
        private String classQualifiedName = null;
        private String methodSimpleName = null;
        private int line = -1;

        // override this method
        public void replace(
                String classQualifiedName, String methodSimpleName, int line) {
            this.classQualifiedName = classQualifiedName;
            this.methodSimpleName = methodSimpleName;
            this.line = line;
        }

        public final String getReplacedClassQualifiedName() {
            return classQualifiedName;
        }

        public final void replaceClassQualifiedName(String classQualifiedName) {
            this.classQualifiedName = classQualifiedName;
        }

        public final String getReplacedMethodSimpleName() {
            return methodSimpleName;
        }

        public final void replaceMethodSimpleName(String methodSimpleName) {
            this.methodSimpleName = methodSimpleName;
        }

        public final int getReplacedLine() {
            return line;
        }

        public final void replaceLine(int line) {
            this.line = line;
        }
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

    private static StackLine getStackLine(SrcTree srcTree,
            StackTraceElement element, LineReplacer replacer) {
        replacer.replace(element.getClassName(), element.getMethodName(), element.getLineNumber());
        return getStackLine(srcTree, replacer.getReplacedClassQualifiedName(),
                replacer.getReplacedMethodSimpleName(), replacer.getReplacedLine());
    }

    // gap line (the line out of SrcTree) is skipped
    public static List<StackLine> getStackLines(SrcTree srcTree,
            StackTraceElement[] elements, LineReplacer replacer) {
        List<StackLine> stackLines = new ArrayList<StackLine>(elements.length);
        for (StackTraceElement element : elements) {
            StackLine stackLine = getStackLine(srcTree, element, replacer);
            if (stackLine != null) {
                stackLines.add(stackLine);
            }
        }
        return stackLines;
    }
}
