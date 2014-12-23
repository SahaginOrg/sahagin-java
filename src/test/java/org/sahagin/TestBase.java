package org.sahagin;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

public abstract class TestBase {
    private static final String TEST_JAVA_RESOURCE_ROOT = "src/test/java";
    private static final String TEST_RESOURCE_ROOT = "src/test/resources/";
    private static final String WORK_ROOT = "work/";

    private String classFullPath() {
        return this.getClass().getCanonicalName().replace(".", "/");
    }

    // test input java files located not on resource directory but on test directory
    public final File testJavaResourceDir(String methodName) {
        return new File(TEST_JAVA_RESOURCE_ROOT, classFullPath() + "Res/" + methodName);
    }

    public final File testResourceDir(String methodName) {
        return new File(TEST_RESOURCE_ROOT, classFullPath() + "Res/" + methodName);
    }

    public final File mkWorkDir(String methodName) {
        File workDir = new File(WORK_ROOT, classFullPath() + "Res/" + methodName);
        workDir.mkdirs();
        return workDir;
    }

    public static void assertFileContentsEquals(File expected, File actual) {
        List<String> expectedLines;
        List<String> actualLines;
        try {
            expectedLines = FileUtils.readLines(expected, "UTF-8");
            actualLines = FileUtils.readLines(actual, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int min = Math.min(actualLines.size(), expectedLines.size());
        for (int i = 0; i < min; i++) {
            assertThat(String.format(
                    "src tree mismatch%n expectedLine: %s%n actualLine: %s%n line %d%n expected: %s%n actual: %s",
                    expectedLines.get(i), actualLines.get(i), i,
                    expected.getAbsolutePath(), actual.getAbsolutePath()),
                    actualLines.get(i), is(expectedLines.get(i)));
        }
        if (expectedLines.size() > min) {
            fail(String.format(
                    "src tree mismatch%n expectedLine: %s%n line %d%n expected: %s%n actual: %s",
                    expectedLines.get(min), min,
                    expected.getAbsolutePath(), actual.getAbsolutePath()));
        }
        if (actualLines.size() > min) {
            fail(String.format(
                    "src tree mismatch%n actualLine: %s%n line %d%n expected: %s%n actual: %s",
                    actualLines.get(min), min,
                    expected.getAbsolutePath(), actual.getAbsolutePath()));
        }
    }
}
