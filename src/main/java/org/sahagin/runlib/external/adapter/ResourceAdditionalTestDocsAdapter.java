package org.sahagin.runlib.external.adapter;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.sahagin.runlib.additionaltestdoc.AdditionalClassTestDoc;
import org.sahagin.runlib.additionaltestdoc.AdditionalMethodTestDoc;
import org.sahagin.runlib.additionaltestdoc.AdditionalPage;
import org.sahagin.runlib.additionaltestdoc.AdditionalTestDocs;
import org.sahagin.runlib.external.CaptureStyle;
import org.sahagin.runlib.external.Locale;
import org.sahagin.share.AcceptableLocales;
import org.sahagin.share.yaml.YamlConvertException;
import org.sahagin.share.yaml.YamlUtils;

public abstract class ResourceAdditionalTestDocsAdapter
implements AdditionalTestDocsAdapter {
    private AdditionalTestDocs docs;
    private AcceptableLocales locales;
    private Map<Locale, Map<String, Object>> localeClassYamlObjMap;
    private Map<Locale, Map<String, Object>> localeMethodYamlObjMap;

    // Path from the jar or project file top level.
    // This path must not end with "/"
    public abstract String resourceDirPath();

    // list of locale and its YAML object pair
    private void setLocaleYamlObjListFromResource() throws YamlConvertException {
        localeClassYamlObjMap = new HashMap<Locale, Map<String, Object>>(8);
        localeMethodYamlObjMap = new HashMap<Locale, Map<String, Object>>(8);
        for (Locale locale : Locale.values()) {
            InputStream in = this.getClass().getResourceAsStream(
                    resourceDirPath() + "/" + locale.getValue() + ".yml");
            if (in == null) {
                // language resource does not exist
                continue;
            }
            try {
                Map<String, Object> yamlObj = YamlUtils.load(in);
                Map<String, Object> classYamlObj = YamlUtils.getYamlObjectValue(yamlObj, "class", true);
                Map<String, Object> methodYamlObj = YamlUtils.getYamlObjectValue(yamlObj, "method", true);
                if (classYamlObj != null) {
                    localeClassYamlObjMap.put(locale, classYamlObj);
                }
                if (methodYamlObj != null) {
                    localeMethodYamlObjMap.put(locale, methodYamlObj);
                }
            } finally {
                IOUtils.closeQuietly(in);
            }
        }
    }

    @Override
    public final void add(AdditionalTestDocs docs, AcceptableLocales locales) {
        this.docs = docs;
        this.locales = locales;
        try {
            setLocaleYamlObjListFromResource();
        } catch (YamlConvertException e) {
            throw new RuntimeException(e);
        }
        classAdd();
        methodAdd();
    }

    private void classAddSub(AdditionalClassTestDoc classTestDocInstance, String qualifiedName) {
        classTestDocInstance.setQualifiedName(qualifiedName);
        String testDoc = ""; // set empty string if no locale data is found
        for (Locale locale : locales.getLocales()) {
            Map<String, Object> map = localeClassYamlObjMap.get(locale);
            if (map == null) {
                continue;
            }
            Object value = map.get(qualifiedName);
            if (value != null) {
                testDoc = (String) value;
                break;
            }
        }
        classTestDocInstance.setTestDoc(testDoc);
        docs.classAdd(classTestDocInstance);
    }

    protected final void classAdd(String qualifiedName) {
        classAddSub(new AdditionalClassTestDoc(), qualifiedName);
    }

    protected final void classAdd(String qualifiedName, String delegateToQualifiedName) {
        AdditionalClassTestDoc testDoc = new AdditionalClassTestDoc();
        testDoc.setDelegateToQualifiedName(delegateToQualifiedName);
        classAddSub(testDoc, qualifiedName);
    }

    protected final void pageAdd(String qualifiedName) {
        classAddSub(new AdditionalPage(), qualifiedName);
    }

    protected final void pageAdd(String qualifiedName, String delegateToQualifiedName) {
        AdditionalPage testDoc = new AdditionalPage();
        testDoc.setDelegateToQualifiedName(delegateToQualifiedName);
        classAddSub(testDoc, qualifiedName);
    }

    public abstract void classAdd();

    // argClassesStr.. null means no overload
    protected final void methodAdd(String classQualifiedName, String methodSimpleName) {
        methodAdd(classQualifiedName, methodSimpleName,
                null, -1, CaptureStyle.getDefault());
    }

    // argClassesStr.. null means no overload
    protected final void methodAdd(String classQualifiedName,
            String methodSimpleName, String argClassesStr) {
        methodAdd(classQualifiedName, methodSimpleName,
                argClassesStr, -1, CaptureStyle.getDefault());
    }

    protected final void methodAdd(String classQualifiedName,
            String methodSimpleName, int varLengthArgIndex) {
        methodAdd(classQualifiedName, methodSimpleName,
                null, varLengthArgIndex, CaptureStyle.getDefault());
    }

    protected final void methodAdd(String classQualifiedName,
            String methodSimpleName, CaptureStyle captureStyle) {
        methodAdd(classQualifiedName, methodSimpleName,
                null, -1, captureStyle);
    }

    // argClassesStr.. null means no overload
    protected final void methodAdd(String classQualifiedName,
            String methodSimpleName, String argClassesStr, int varLengthArgIndex) {
        methodAdd(classQualifiedName, methodSimpleName,
                argClassesStr, varLengthArgIndex, CaptureStyle.getDefault());
    }

    // argClassesStr.. null means no overload
    protected final void methodAdd(String classQualifiedName,
            String methodSimpleName, String argClassesStr, CaptureStyle captureStyle) {
        methodAdd(classQualifiedName, methodSimpleName,
                argClassesStr, -1, captureStyle);
    }


    // argClassesStr.. null means no overload
    // TODO when qualified String or Object class name is used in argClassesStr
    protected final void methodAdd(String classQualifiedName,
            String methodSimpleName, String argClassesStr,
            int varLengthArgIndex, CaptureStyle captureStyle) {
        AdditionalMethodTestDoc methodTestDocInstance = new AdditionalMethodTestDoc();
        methodTestDocInstance.setClassQualifiedName(classQualifiedName);
        methodTestDocInstance.setSimpleName(methodSimpleName);
        methodTestDocInstance.setCaptureStyle(captureStyle);
        methodTestDocInstance.setVariableLengthArgIndex(varLengthArgIndex);
        if (argClassesStr != null) {
            methodTestDocInstance.setOverload(argClassesStr);
        } else {
            methodTestDocInstance.setNotOverload();
        }
        String testDoc = ""; // set empty string if no locale data is found
        String methodQualifiedName = classQualifiedName + "." + methodSimpleName;
        for (Locale locale : locales.getLocales()) {
            Map<String, Object> map = localeMethodYamlObjMap.get(locale);
            if (map == null) {
                continue;
            }
            Object value = map.get(methodQualifiedName);
            if (value != null) {
                if (argClassesStr == null && value instanceof String) {
                    // no overload
                    testDoc = (String) value;
                    break;
                } else if (argClassesStr != null && value instanceof Map) {
                    // overload
                    @SuppressWarnings("unchecked")
                    Map<String, Object> overloadMap = (Map<String, Object>) value;
                    Object overloadValue = overloadMap.get(argClassesStr);
                    if (overloadValue != null) {
                        testDoc = (String) overloadValue;
                        break;
                    }
                }
            }
        }
        methodTestDocInstance.setTestDoc(testDoc);
        docs.methodAdd(methodTestDocInstance);
    }

    public abstract void methodAdd();

}
