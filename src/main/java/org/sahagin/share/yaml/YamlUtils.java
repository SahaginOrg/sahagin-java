package org.sahagin.share.yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.io.IOUtils;
import org.sahagin.runlib.external.CaptureStyle;
import org.sahagin.runlib.external.Locale;
import org.yaml.snakeyaml.Yaml;

public class YamlUtils {
    private static final String MSG_KEY_NOT_FOUND = "key \"%s\" is not found";
    private static final String MSG_MUST_BE_BOOLEAN
    = "value for \"%s\" must be \"true\" or \"false\", but is \"%s\"";
    private static final String MSG_VALUE_NOT_INT = "can't convert value to int; key: %s; vaule: %s";
    private static final String MSG_VALUE_NOT_CAPTURE_STYLE
    = "can't convert value to CaptureStyle; key: %s; vaule: %s";
    private static final String MSG_VALUE_NOT_LOCALE
    = "can't convert value to Locale; key: %s; vaule: %s";
    private static final String MSG_NOT_EQUALS_TO_EXPECTED = "\"%s\" is not equals to \"%s\"";
    private static final String MSG_LIST_MUST_NOT_BE_NULL = "list must not be null";

    // if allowsEmpty and key entry is not found, just returns null.
    // (null may mean null value for the specified key)
    public static Object getObjectValue(Map<String, Object> yamlObject, String key, boolean allowsEmpty)
            throws YamlConvertException {
        if (yamlObject == null) {
            throw new NullPointerException();
        }
        Object obj = yamlObject.get(key);
        if (obj == null && !yamlObject.containsKey(key) && !allowsEmpty) {
            throw new YamlConvertException(String.format(MSG_KEY_NOT_FOUND, key));
        }
        return obj;
    }

    public static Object getObjectValue(Map<String, Object> yamlObject, String key)
            throws YamlConvertException {
        return getObjectValue(yamlObject, key, false);
    }

    // if allowsEmpty, returns null for the case no key entry or null value
    public static Boolean getBooleanValue(Map<String, Object> yamlObject, String key, boolean allowsEmpty)
            throws YamlConvertException {
        Object obj = getObjectValue(yamlObject, key, allowsEmpty);
        if (obj == null) {
            if (allowsEmpty) {
                return null;
            } else {
                throw new YamlConvertException(String.format(MSG_MUST_BE_BOOLEAN, key, obj));
            }
        } else if (obj.toString().equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        } else if (obj.toString().equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        } else {
            throw new YamlConvertException(String.format(MSG_MUST_BE_BOOLEAN, key, obj));
        }
    }

    public static Boolean getBooleanValue(Map<String, Object> yamlObject, String key)
            throws YamlConvertException {
        return getBooleanValue(yamlObject, key, false);
    }

    public static String getStrValue(Map<String, Object> yamlObject, String key, boolean allowsEmpty)
            throws YamlConvertException {
        Object obj = getObjectValue(yamlObject, key, allowsEmpty);
        if (obj == null) {
            return null;
        } else {
            return obj.toString();
        }
    }

    public static String getStrValue(Map<String, Object> yamlObject, String key)
            throws YamlConvertException {
        return getStrValue(yamlObject, key, false);
    }

    public static void strValueEqualsCheck(Map<String, Object> yamlObject,
            String key, String expected, String defaultValue)
            throws YamlConvertException {
        String value = getStrValue(yamlObject, key, true);
        if (value == null) {
            value = defaultValue;
        }
        if (!StringUtils.equals(value, expected)) {
            throw new YamlConvertException(String.format(
                    MSG_NOT_EQUALS_TO_EXPECTED, value, expected));
        }
    }

    public static void strValueEqualsCheck(Map<String, Object> yamlObject, String key, String expected)
            throws YamlConvertException {
        String value = getStrValue(yamlObject, key);
        if (!StringUtils.equals(value, expected)) {
            throw new YamlConvertException(String.format(
                    MSG_NOT_EQUALS_TO_EXPECTED, value, expected));
        }
    }

    // if allowsEmpty, returns null for the case no key entry or null value
    public static Integer getIntValue(Map<String, Object> yamlObject, String key, boolean allowsEmpty)
            throws YamlConvertException {
        Object obj = getObjectValue(yamlObject, key, allowsEmpty);
        if (obj == null && allowsEmpty) {
            return null;
        }
        String objStr;
        if (obj == null) {
            objStr = null;
        } else {
            objStr = obj.toString();
        }
        try {
            return new Integer(objStr);
        } catch (NumberFormatException e) {
            throw new YamlConvertException(String.format(MSG_VALUE_NOT_INT, key, objStr));
        }
    }

    public static Integer getIntValue(Map<String, Object> yamlObject, String key)
            throws YamlConvertException {
        return getIntValue(yamlObject, key, false);
    }

    // if allowsEmpty, returns null for the case no key entry or null value
    public static CaptureStyle getCaptureStyleValue(Map<String, Object> yamlObject,
            String key, boolean allowsEmpty) throws YamlConvertException {
        Object obj = getObjectValue(yamlObject, key, allowsEmpty);
        if (obj == null && allowsEmpty) {
            return null;
        }
        String objStr;
        if (obj == null) {
            objStr = null;
        } else {
            objStr = obj.toString();
        }
        CaptureStyle result = CaptureStyle.getEnum(objStr);
        if (result != null) {
            return result;
        } else {
            throw new YamlConvertException(String.format(MSG_VALUE_NOT_CAPTURE_STYLE, key, objStr));
        }
    }

    public static CaptureStyle getCaptureStyleValue(Map<String, Object> yamlObject,
            String key) throws YamlConvertException {
        return getCaptureStyleValue(yamlObject, key, false);
    }

    public static Locale getLocaleValue(Map<String, Object> yamlObject, String key, boolean allowsEmpty)
            throws YamlConvertException {
        Object obj = getObjectValue(yamlObject, key, allowsEmpty);
        if (obj == null && allowsEmpty) {
            return null;
        }
        String objStr;
        if (obj == null) {
            objStr = null;
        } else {
            objStr = obj.toString();
        }
        Locale result = Locale.getEnum(objStr);
        if (result != null) {
            return result;
        } else {
            throw new YamlConvertException(String.format(MSG_VALUE_NOT_LOCALE, key, objStr));
        }
    }

    public static Locale getLocaleValue(Map<String, Object> yamlObject,
            String key) throws YamlConvertException {
        return getLocaleValue(yamlObject, key, false);
    }

    // returns null for empty
    public static Map<String, Object> getYamlObjectValue(Map<String, Object> yamlObject,
            String key, boolean allowsEmpty) throws YamlConvertException {
        Object obj = getObjectValue(yamlObject, key, allowsEmpty);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) obj;
        return result;
    }

    // for null or not found, returns null
    public static Map<String, Object> getYamlObjectValue(Map<String, Object> yamlObject,
            String key) throws YamlConvertException {
        return getYamlObjectValue(yamlObject, key, false);
    }

    // for null or not found key, returns empty list
    public static List<String> getStrListValue(Map<String, Object> yamlObject, String key,
            boolean allowsEmpty) throws YamlConvertException {
        Object obj = getObjectValue(yamlObject, key, allowsEmpty);
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) obj;
        if (result == null) {
            if (!allowsEmpty) {
                throw new YamlConvertException(MSG_LIST_MUST_NOT_BE_NULL);
            }
            result = new ArrayList<>(0);
        }
        return result;
    }

    public static List<String> getStrListValue(Map<String, Object> yamlObject, String key)
            throws YamlConvertException {
        return getStrListValue(yamlObject, key, false);
    }

    // for null or not found key, returns empty list
    public static List<Integer> getIntListValue(Map<String, Object> yamlObject, String key,
            boolean allowsEmpty) throws YamlConvertException {
        Object obj = getObjectValue(yamlObject, key, allowsEmpty);
        @SuppressWarnings("unchecked")
        List<Integer> result = (List<Integer>) obj;
        if (result == null) {
            if (!allowsEmpty) {
                throw new YamlConvertException(MSG_LIST_MUST_NOT_BE_NULL);
            }
            result = new ArrayList<>(0);
        }
        return result;
    }

    public static List<Integer> getIntListValue(Map<String, Object> yamlObject, String key)
            throws YamlConvertException {
        return getIntListValue(yamlObject, key, false);
    }

    public static Map<String, Object> toYamlObject(YamlConvertible src) {
        if (src == null) {
            return null;
        } else {
            return src.toYamlObject();
        }
    }

    // for null or not found key, returns empty list
    public static List<Map<String, Object>> getYamlObjectListValue(Map<String, Object> yamlObject,
            String key, boolean allowsEmpty) throws YamlConvertException {
        Object obj = getObjectValue(yamlObject, key, allowsEmpty);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = (List<Map<String, Object>>) obj;
        if (result == null) {
            if (!allowsEmpty) {
                throw new YamlConvertException(MSG_LIST_MUST_NOT_BE_NULL);
            }
            result = new ArrayList<>(0);
        }
        return result;
    }

    public static List<Map<String, Object>> getYamlObjectListValue(Map<String, Object> yamlObject,
            String key) throws YamlConvertException {
        return getYamlObjectListValue(yamlObject, key, false);
    }

    public static <T extends YamlConvertible> List<Map<String, Object>> toYamlObjectList(List<T> srcList) {
        List<Map<String, Object>> result = new ArrayList<>(srcList.size());
        for (T src : srcList) {
            Map<String, Object> yamlObj = toYamlObject(src);
            result.add(yamlObj);
        }
        return result;
    }

    // this method does not close the stream
    public static Map<String, Object> load(InputStream input) {
        Yaml yaml = new Yaml();
        Object rawYamlObj = yaml.load(input);
        @SuppressWarnings("unchecked")
        Map<String, Object> yamlObj = (Map<String, Object>) rawYamlObj;
        return yamlObj;
    }

    public static Map<String, Object> load(File yamlFile) {
        FileInputStream input = null;
        Map<String, Object> result = null;
        try {
            input = new FileInputStream(yamlFile);
            result = load(input);
            input.close();
        } catch (IOException e) {
            throw new RuntimeException("exception for " + yamlFile.getAbsolutePath(), e);
        } finally {
            IOUtils.closeQuietly(input);
        }
        return result;
    }

    public static void dump(Map<String, Object> yamlObj, File dumpFile) {
        if (dumpFile.getParentFile() != null) {
            dumpFile.getParentFile().mkdirs();
        }
        Yaml yaml = new Yaml();
        FileWriterWithEncoding writer = null;
        try {
            writer = new FileWriterWithEncoding(dumpFile, Charsets.UTF_8);
            yaml.dump(yamlObj, writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }
}
