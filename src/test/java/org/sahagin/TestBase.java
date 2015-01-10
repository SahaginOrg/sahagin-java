package org.sahagin;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;

public abstract class TestBase {
    private static final String TEST_RESOURCE_ROOT = "src/test/resources/";
    private static final String WORK_ROOT = "work/";

    private String classFullPath() {
        return this.getClass().getCanonicalName().replace(".", "/");
    }

    public final File testResourceDir() {
        return new File(TEST_RESOURCE_ROOT, classFullPath() + "Res");
    }

    public final File testResourceDir(String methodName) {
        return new File(TEST_RESOURCE_ROOT, classFullPath() + "Res/" + methodName);
    }

    public final void clearWorkDir() {
        File workDir = new File(WORK_ROOT, classFullPath() + "Res");
        try {
            FileUtils.deleteDirectory(workDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public final File mkWorkDir() {
        File workDir = new File(WORK_ROOT, classFullPath() + "Res");
        workDir.mkdirs();
        return workDir;
    }

    public final void clearWorkDir(String methodName) {
        File workDir = new File(WORK_ROOT, classFullPath() + "Res/" + methodName);
        try {
            FileUtils.deleteDirectory(workDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public final File mkWorkDir(String methodName) {
        File workDir = new File(WORK_ROOT, classFullPath() + "Res/" + methodName);
        workDir.mkdirs();
        return workDir;
    }

    public static void assertFileByteContentsEquals(File expected, File actual) {
        assertTrue(expected + " does not exist", expected.exists());
        assertTrue(actual + " does not exist", actual.exists());
        byte[] expectedBytes;
        byte[] actualBytes;
        try {
            expectedBytes = FileUtils.readFileToByteArray(expected);
            actualBytes = FileUtils.readFileToByteArray(actual);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertThat("expected: " + expected + "; actual: " + actual,
                expectedBytes, is(actualBytes));
    }

    // TODO define custom matcher for file text
    public static void assertFileTextContentsEquals(File expected, File actual) {
        assertTrue(expected.exists());
        assertTrue(actual.exists());
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

    // null means not YAML object or null
    private Map<String, Object> toYamlObj(Object value) {
        try {
            @SuppressWarnings({"unchecked"})
            Map<String, Object> valueAsMap = (Map<String, Object>) value;
            return valueAsMap;
        } catch (ClassCastException e) {
            return null;
        }
    }

    // null means not list or null
    private List<Object> toList(Object value) {
        try {
            @SuppressWarnings({"unchecked"})
            List<Object> valueAsList = (List<Object>) value;
            return valueAsList;
        } catch (ClassCastException e) {
            return null;
        }
    }

    private void assertYamlEachValueEquals(Object expectedValue,
            Object actualValue, String keyPath) {
        Map<String, Object> expectedValueAsYamlObj = toYamlObj(expectedValue);
        Map<String, Object> actualValueAsYamlObj = toYamlObj(actualValue);
        if (expectedValueAsYamlObj != null && actualValueAsYamlObj != null) {
            assertYamlEqualsSub(expectedValueAsYamlObj, actualValueAsYamlObj, keyPath);
            return;
        }

        List<Object> expectedValueAsList = toList(expectedValue);
        List<Object> actualValueAsList = toList(actualValue);
        if (expectedValueAsList != null && actualValueAsList != null) {
            assertThat(keyPath, actualValueAsList.size(), is(expectedValueAsList.size()));
            for (int i = 0; i < expectedValueAsList.size(); i++) {
                assertYamlEachValueEquals(expectedValueAsList.get(i), actualValueAsList.get(i), keyPath + "[" + i + "]");
            }
            return;
        }

        if (expectedValue == null) {
            assertThat(keyPath, expectedValue, is(nullValue()));
        } else if (expectedValue instanceof String) {
            String regExp = toRegExp((String) expectedValue);
            if (actualValue == null || !actualValue.toString().matches(regExp)) {
                fail(keyPath + "; expected: " + expectedValue + "; actual: " + actualValue);
            }
        } else  {
            assertThat(keyPath, actualValue, is(expectedValue));
        }
    }

    private void assertYamlEqualsSub(Map<String, Object> expected,
            Map<String, Object> actual, String keyPath) {
        if (expected == null) {
            assertThat(keyPath, actual, is(nullValue()));
        } else {
            assertThat(keyPath, actual, is(notNullValue()));
        }
        assertThat(keyPath, actual.size(), is(expected.size()));
        for (Map.Entry<String, Object> entry : expected.entrySet()) {
            assertTrue(keyPath, actual.containsKey(entry.getKey()));
            Object expectedValue = expected.get(entry.getKey());
            Object actualValue = actual.get(entry.getKey());
            String newKeyPath;
            if (keyPath == null) {
                newKeyPath = entry.getKey();
            } else {
                newKeyPath = keyPath + ">" + entry.getKey();
            }
            assertYamlEachValueEquals(expectedValue, actualValue, newKeyPath);
        }
    }

    // TODO define custom matcher for YAML
    public void assertYamlEquals(Map<String, Object> expected, Map<String, Object> actual) {
        assertYamlEqualsSub(expected, actual, null);
    }

    private String toRegExp(String normalStringContainingWildcard) {
        // by (?s) flag, this expression matches multiple lines string
        return "(?s)\\Q" + normalStringContainingWildcard.replace("*", "\\E.*\\Q") + "\\E";
    }
}
