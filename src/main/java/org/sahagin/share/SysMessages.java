package org.sahagin.share;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.sahagin.runlib.external.Locale;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlUtils;

public class SysMessages {
    public static final String CODE_LINE_WITHOUT_TEST_DOC = "codeLineWithoutTestDoc";
    public static final String JS_LOCAL_VAR = "jsLocalVar";
    public static final String JS_LOCAL_VAR_ASSIGN = "jsLocalVarAssign";
    public static final String LOCAL_VAR = "localVar";
    public static final String LOCAL_VAR_ASSIGN = "localVarAssign";
    public static final String REPORT_HIDE_CODE = "reportHideCode";
    public static final String REPORT_SHOW_CODE = "reportShowCode";

    // list of locale and its YAML object pair
    private Map<Locale, Map<String, Object>> localeYamlObjMap;
    private AcceptableLocales locales;
    private static SysMessages globalInstance = null;

    private void loadFromResource(AcceptableLocales locales) throws YamlConvertException {
        this.locales = locales;
        localeYamlObjMap = new HashMap<Locale, Map<String, Object>>(8);
        for (Locale locale : this.locales.getLocales()) {
            InputStream in = this.getClass().getResourceAsStream(
                    CommonPath.standardSystemLocaleResDirPath("java")
                    + "/" + locale.getValue() + ".yml");
            if (in == null) {
                // language resource does not exist
                continue;
            }
            try {
                Map<String, Object> yamlObj = YamlUtils.load(in);
                if (yamlObj != null) {
                    localeYamlObjMap.put(locale, yamlObj);
                }
            } finally {
                IOUtils.closeQuietly(in);
            }
        }
    }

    private String getMessage(String key) {
        for (Locale locale : locales.getLocales()) {
            Map<String, Object> map = localeYamlObjMap.get(locale);
            if (map == null) {
                continue;
            }
            Object value = map.get(key);
            if (value != null) {
                return (String) value;
            }
        }
        throw new IllegalArgumentException("message not found: " + key);
    }

    public static void globalInitialize(AcceptableLocales locales) {
        globalInstance = new SysMessages();
        try {
            globalInstance.loadFromResource(locales);
        } catch (YamlConvertException e) {
            throw new RuntimeException("invalid resource file", e);
        }
    }

    public static String get(String key) {
        if (globalInstance == null) {
            throw new IllegalStateException("not initialized");
        }
        return globalInstance.getMessage(key);
    }

}
