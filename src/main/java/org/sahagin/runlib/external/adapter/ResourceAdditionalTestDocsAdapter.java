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
    private Map<Locale, Map<String, Object>> localeFuncYamlObjMap;

    // Path from the jar or project file top level.
    // This path must not end with "/"
    public abstract String resourceDirPath();

    // list of locale and its YAML object pair
    private void setLocaleYamlObjListFromResource() throws YamlConvertException {
        localeClassYamlObjMap = new HashMap<Locale, Map<String, Object>>(8);
        localeFuncYamlObjMap = new HashMap<Locale, Map<String, Object>>(8);
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
                Map<String, Object> funcYamlObj = YamlUtils.getYamlObjectValue(yamlObj, "func", true);
                if (classYamlObj != null) {
                    localeClassYamlObjMap.put(locale, classYamlObj);
                }
                if (funcYamlObj != null) {
                    localeFuncYamlObjMap.put(locale, funcYamlObj);
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
        funcAdd();
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

    protected final void pageAdd(String qualifiedName) {
        classAddSub(new AdditionalPage(), qualifiedName);
    }

    public abstract void classAdd();

    protected final void methodAdd(String classQualifiedName, 
            String methodSimpleName, CaptureStyle captureStyle) {
        AdditionalMethodTestDoc methodTestDocInstance = new AdditionalMethodTestDoc();
        String methodQualifiedName = classQualifiedName + "." + methodSimpleName;
        methodTestDocInstance.setClassQualifiedName(classQualifiedName);
        methodTestDocInstance.setQualifiedName(methodQualifiedName);
        methodTestDocInstance.setCaptureStyle(captureStyle);
        String testDoc = ""; // set empty string if no locale data is found
        for (Locale locale : locales.getLocales()) {
            Map<String, Object> map = localeFuncYamlObjMap.get(locale);
            if (map == null) {
                continue;
            }
            Object value = map.get(methodQualifiedName);
            if (value != null) {
                testDoc = (String) value;
                break;
            }
        }
        methodTestDocInstance.setTestDoc(testDoc);
        docs.funcAdd(methodTestDocInstance);
    }

    protected final void methodAdd(String classQualifiedName, String methodSimpleName) {
        methodAdd(classQualifiedName, methodSimpleName, CaptureStyle.getDefault());
    }

    public abstract void funcAdd();

}
