package org.sahagin.share;

import org.sahagin.share.srctree.SrcTree;
import org.sahagin.share.srctree.TestClass;
import org.sahagin.share.srctree.TestMethod;

public class SrcTreeChecker {
    // TODO throw error if sub class has root method
    // TODO throw error if class for root method is not root class
    // TODO throw error if root class is page class
    // TODO throw error if captureStyle value is defined on @TestDoce inside the @TestDocs
    // TODO throw error if @TestDoc and @TestDocs are defined on the same place
    // TODO throw error if @TestDoc and @Page are defined on the same place
    // TODO throw error if @TestDocs value array is empty

    private static final String MSG_INVALID_PLACEHOLDER
    = "TestDoc of \"%s\" contains invalid keyword \"%s\"";

    public static void check(SrcTree srcTree) throws IllegalTestScriptException {
        for (TestMethod method : srcTree.getRootMethodTable().getTestMethods()) {
            if (method.getTestDoc() != null && method.getTestDoc().contains("\\")) {
                // TODO support back slash
                throw new RuntimeException(String.format(
                        "back slash is not supported: %s", method.getTestDoc()));
            }
            String invalidKeyword = TestDocResolver.searchInvalidPlaceholder(method);
            if (invalidKeyword != null) {
                throw new IllegalTestScriptException(String.format(MSG_INVALID_PLACEHOLDER,
                        method.getQualifiedName(), invalidKeyword));
            }
        }
        for (TestMethod method : srcTree.getSubMethodTable().getTestMethods()) {
            if (method.getTestDoc() != null && method.getTestDoc().contains("\\")) {
                // TODO support back slash
                throw new RuntimeException(String.format(
                        "back slash is not supported: %s", method.getTestDoc()));
            }
            String invalidKeyword = TestDocResolver.searchInvalidPlaceholder(method);
            if (invalidKeyword != null) {
                throw new IllegalTestScriptException(String.format(MSG_INVALID_PLACEHOLDER,
                        method.getQualifiedName(), invalidKeyword));
            }
        }
        for (TestClass testClass: srcTree.getRootClassTable().getTestClasses()) {
            if (testClass.getTestDoc() != null && testClass.getTestDoc().contains("\\")) {
                // TODO support back slash
                throw new RuntimeException(String.format(
                        "back slash is not supported: %s", testClass.getTestDoc()));
            }
        }
        for (TestClass testClass: srcTree.getSubClassTable().getTestClasses()) {
            if (testClass.getTestDoc() != null && testClass.getTestDoc().contains("\\")) {
                // TODO support back slash
                throw new RuntimeException(String.format(
                        "back slash is not supported: %s", testClass.getTestDoc()));
            }
        }
    }
}
