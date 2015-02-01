package org.sahagin.runlib.additionaltestdoc;

import org.sahagin.runlib.external.CaptureStyle;

public class AdditionalMethodTestDoc {
    private String classQualifiedName;
    private String simpleName;
    // null means not overloaded
    private String argClassesStr = null;
    private int variableLengthArgIndex = -1;
    private String testDoc;
    // TODO should not allow stepInCapture for AdditionalTestDoc
    private CaptureStyle captureStyle = CaptureStyle.getDefault();

    public String getClassQualifiedName() {
        return classQualifiedName;
    }

    public void setClassQualifiedName(String classQualifiedName) {
        this.classQualifiedName = classQualifiedName;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    public String getQualifiedName() {
        if (classQualifiedName == null || simpleName == null) {
            return simpleName;
        } else {
            return classQualifiedName + "." + simpleName;
        }
    }

    public boolean isOverloaded() {
        return argClassesStr != null;
    }

    public String getArgClassesStr() {
        if (!isOverloaded()) {
            throw new IllegalStateException(
                    "not overloaded method, and argument information is not set");
        }
        return argClassesStr;
    }

    public void setNotOverload() {
        argClassesStr = null;
    }

    public void setOverload(String argClassesStr) {
        if (argClassesStr == null) {
            throw new NullPointerException();
        }
        this.argClassesStr = argClassesStr;
    }

    public boolean hasVariableLengthArg() {
        return variableLengthArgIndex != -1;
    }

    public int getVariableLengthArgIndex() {
        return variableLengthArgIndex;
    }

    public void setVariableLengthArgIndex(int variableLengthArgIndex) {
        this.variableLengthArgIndex = variableLengthArgIndex;
    }

    public String getTestDoc() {
        return testDoc;
    }

    public void setTestDoc(String testDoc) {
        this.testDoc = testDoc;
    }

    public CaptureStyle getCaptureStyle() {
        return captureStyle;
    }

    public void setCaptureStyle(CaptureStyle captureStyle) {
        this.captureStyle = captureStyle;
    }

    /*
    public static String generateMethodKey(AdditionalMethodTestDoc testDoc) {
        if (testDoc == null) {
            throw new NullPointerException();
        }
        if (testDoc.isOverloaded()) {
            return generateMethodKey(testDoc.getClassQualifiedName(),
                    testDoc.getSimpleName(), testDoc.getArgClassQualifiedNames());
        } else {
            return generateMethodKey(
                    testDoc.getClassQualifiedName(), testDoc.getSimpleName());
        }
    }

    public static String generateMethodKey(String classQualifiedName,
            String methodSimpleName, String argClassesStr) {
        if (argClassesStr == null) {
            throw new NullPointerException();
        }
        // TODO should use the same rule as getKey method in jdt
        return "_Additional_" + classQualifiedName + "." + methodSimpleName + "_" + argClassesStr;
    }

    // TODO this method should be removed
    public static boolean isAdditionalMethodKey(String methodKey) {
        return methodKey != null && methodKey.startsWith("_Additional_");
    }

    public static String generateMethodKey(String classQualifiedName,
            String methodSimpleName, List<String> argClassQualifiedNames) {
        if (argClassQualifiedNames == null) {
            throw new NullPointerException();
        }
        // TODO should use the same rule as getKey method in jdt
        return "_Additional_" + classQualifiedName + "."
        + methodSimpleName + "_" + argClassQualifiedNamesToStr(argClassQualifiedNames);
    }

    public static String generateMethodKey(String classQualifiedName, String methodSimpleName) {
        // TODO should use the same rule as getKey method in jdt
        return "_Additional_" + classQualifiedName + "." + methodSimpleName;
    }

    // convert to argClassQualifiedNames
    // "void" means no argument, "String" means java.lang.String,
    // "Object" means java.lang.Object
    private static List<String> argClassesStrToList(String argClassesStr) {
        if (argClassesStr == null) {
            throw new NullPointerException();
        }
        String trimmed = argClassesStr.trim();
        if (trimmed.equals("") || trimmed.equals("void")) {
            return new ArrayList<String>(0);
        }

        String[] splitted = trimmed.split(",");
        List<String> result = new ArrayList<String>(splitted.length);
        for (String argClassStr : splitted) {
            if (StringUtils.equals(argClassStr, String.class.getSimpleName())) {
                result.add(String.class.getCanonicalName());
            } else if (StringUtils.equals(argClassesStr, Object.class.getSimpleName())) {
                result.add(Object.class.getCanonicalName());
            } else {
                result.add(argClassStr);
            }
        }
        return result;
    }

    private static String argClassQualifiedNamesToStr(List<String> argClassQualifiedNames) {
        if (argClassQualifiedNames == null) {
            throw new NullPointerException();
        }
        if (argClassQualifiedNames.size() == 0) {
            return "void";
        }
        String result = "";
        for (int i = 0; i < argClassQualifiedNames.size(); i++) {
            if (i != 0) {
                result = result + ",";
            }
            if (StringUtils.equals(argClassQualifiedNames.get(i), String.class.getCanonicalName())) {
                result = result + String.class.getSimpleName();
            } else if (StringUtils.equals(argClassQualifiedNames.get(i), Object.class.getCanonicalName())) {
                result = result + Object.class.getSimpleName();
            } else {
                result = result + argClassQualifiedNames.get(i);
            }
        }
        return result;
    }*/

}
