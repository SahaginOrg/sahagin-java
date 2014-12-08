package org.sahagin.share;

import org.sahagin.share.srctree.SrcTree;
import org.sahagin.share.srctree.TestFunction;

public class SrcTreeChecker {
    // TODO throw error if sub class has root method
    // TODO throw error if class for root method is not root class
    // TODO throw error if root class is page class

    private static final String MSG_INVALID_PLACEHOLDER
    = "TestDoc of \"%s\" contains invalid keyword \"%s\"";

    public static void check(SrcTree srcTree) throws IllegalTestScriptException {
        for (TestFunction func : srcTree.getRootFuncTable().getTestFunctions()) {
            String invalidKeyword = TestDocResolver.searchInvalidPlaceholder(func);
            if (invalidKeyword != null) {
                throw new IllegalTestScriptException(String.format(MSG_INVALID_PLACEHOLDER,
                        func.getQualifiedName(), invalidKeyword));
            }
        }
        for (TestFunction func : srcTree.getSubFuncTable().getTestFunctions()) {
            String invalidKeyword = TestDocResolver.searchInvalidPlaceholder(func);
            if (invalidKeyword != null) {
                throw new IllegalTestScriptException(String.format(MSG_INVALID_PLACEHOLDER,
                        func.getQualifiedName(), invalidKeyword));
            }
        }
    }

}
