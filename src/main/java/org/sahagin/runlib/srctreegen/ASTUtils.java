package org.sahagin.runlib.srctreegen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.sahagin.runlib.external.CaptureStyle;
import org.sahagin.runlib.external.Locale;
import org.sahagin.runlib.external.Page;
import org.sahagin.runlib.external.PageDoc;
import org.sahagin.runlib.external.PageDocs;
import org.sahagin.runlib.external.Pages;
import org.sahagin.runlib.external.TestDoc;
import org.sahagin.runlib.external.TestDocs;
import org.sahagin.share.AcceptableLocales;

@SuppressWarnings("deprecation") // ignore Page and Pages annotation warning
public class ASTUtils {

    // Get the method annotation whose class name is equals  to annotationClass canonical name.
    // Return null if specified name annotation is not found.
    public static IAnnotationBinding getAnnotationBinding(
            IAnnotationBinding[] annotations, String annotationClassName) {
        if (annotations == null) {
            return null;
        }
        if (annotationClassName == null) {
            throw new NullPointerException();
        }

        for (IAnnotationBinding annotation : annotations) {
            String qName = annotation.getAnnotationType().getBinaryName();
            assert qName != null;
            // TODO if multiple annotations for annotationClassName exists
            if (qName.equals(annotationClassName)) {
                return annotation;
            }
        }
        return null;
    }

    // Get the method annotation whose class equals to annotationClass.
    // Return null if specified name annotation is not found.
    private static IAnnotationBinding getAnnotationBinding(
            IAnnotationBinding[] annotations, Class<?> annotationClass) {
        if (annotationClass == null) {
            throw new NullPointerException();
        }
        return getAnnotationBinding(annotations, annotationClass.getCanonicalName());
    }

    // returns null if specified varName annotation is not found
    private static Object getAnnotationValue(IAnnotationBinding annotation, String varName) {
        if (annotation == null) {
            throw new NullPointerException();
        }
        if (varName == null) {
            throw new NullPointerException();
        }
        for (IMemberValuePairBinding value : annotation.getDeclaredMemberValuePairs()) {
            if (value.getName() != null && value.getName().equals(varName)) {
                assert value.getValue() != null; // annotation value cannot be null
                assert !(value.getValue() instanceof IVariableBinding);
                return value.getValue();
            }
        }
        return null;
    }

    // - for example, returns string "STEP_IN" for CaptureStyle.STEP_IN
    // returns null if specified varName annotation is not found
    private static String getEnumAnnotationFieldName(IAnnotationBinding annotation, String varName) {
        if (annotation == null) {
            throw new NullPointerException();
        }
        if (varName == null) {
            throw new NullPointerException();
        }
        for (IMemberValuePairBinding value : annotation.getDeclaredMemberValuePairs()) {
            if (value.getName() != null && value.getName().equals(varName)) {
                assert value.getValue() != null; // annotation value cannot be null
                assert value.getValue() instanceof IVariableBinding;
                IVariableBinding varBinding = (IVariableBinding) value.getValue();
                assert varBinding.isEnumConstant();
                return varBinding.getName();
            }
        }
        return null;
    }

    // returns default value if varName value is not specified
    private static CaptureStyle getAnnotationCaptureStyleValue(
            IAnnotationBinding annotation, String varName) {
        String fieldName = getEnumAnnotationFieldName(annotation, varName);
        if (fieldName == null) {
            return CaptureStyle.getDefault();
        }
        CaptureStyle resultCaptureStyle = CaptureStyle.valueOf(fieldName);
        if (resultCaptureStyle == null) {
            throw new RuntimeException("invalid captureStyle: " + fieldName);
        }
        return resultCaptureStyle;
    }

    // returns default value if varName value is not specified
    private static Locale getAnnotationLocaleValue(
            IAnnotationBinding annotation, String varName) {
        String fieldName = getEnumAnnotationFieldName(annotation, varName);
        if (fieldName == null) {
            return Locale.getDefault();
        }
        Locale resultLocale = Locale.valueOf(fieldName);
        if (resultLocale == null) {
            throw new RuntimeException("invalid locale: " + fieldName);
        }
        return resultLocale;
    }

    // return empty list and default CaptureStyle pair if no TestDoc is found
    private static Pair<Map<Locale, String>, CaptureStyle> getAllTestDocs(
            IAnnotationBinding[] annotations) {
        IAnnotationBinding testDocAnnotation = getAnnotationBinding(annotations, TestDoc.class);
        IAnnotationBinding testDocsAnnotation = getAnnotationBinding(annotations, TestDocs.class);
        if (testDocAnnotation != null && testDocsAnnotation != null) {
            // TODO throw IllegalTestScriptException
            throw new RuntimeException("don't use @TestDoc and @TestDocs at the same place");
        }

        // all @testDoc annotations including annotations contained in @TestDocs
        List<IAnnotationBinding> allTestDocAnnotations = new ArrayList<IAnnotationBinding>(2);
        CaptureStyle resultCaptureStyle = null;

        if (testDocAnnotation != null) {
            // get @TestDoc
            allTestDocAnnotations.add(testDocAnnotation);
            resultCaptureStyle = getAnnotationCaptureStyleValue(testDocAnnotation, "capture");
        } else if (testDocsAnnotation != null) {
            // get @TestDoc from @TestDocs
            Object value = getAnnotationValue(testDocsAnnotation, "value");
            Object[] values = (Object[]) value;
            for (Object element : values) {
                IAnnotationBinding binding = (IAnnotationBinding) element;
                if (getEnumAnnotationFieldName(binding, "capture") != null) {
                    // TODO throw IllegalTestScriptException
                    throw new RuntimeException(
                            "capture must be set on not @TestDoc but @TestDocs");
                }
                allTestDocAnnotations.add(binding);
            }
            resultCaptureStyle = getAnnotationCaptureStyleValue(testDocsAnnotation, "capture");
        }

        // get resultTestDocMap
        Map<Locale, String> resultTestDocMap
        = new HashMap<Locale, String>(allTestDocAnnotations.size());
        for (IAnnotationBinding eachTestDocAnnotation : allTestDocAnnotations) {
            Object value = getAnnotationValue(eachTestDocAnnotation, "value");
            Locale locale = getAnnotationLocaleValue(eachTestDocAnnotation, "locale");
            resultTestDocMap.put(locale, (String) value);
        }

        return Pair.of(resultTestDocMap, resultCaptureStyle);
    }

    // return empty list if no Page is found
    private static Map<Locale, String> getAllPageDocs(IAnnotationBinding[] annotations) {
        // all @PageDoc or @Page annotations including annotations contained in @PageDocs or @Page
        List<IAnnotationBinding> allPageAnnotations = new ArrayList<IAnnotationBinding>(2);

        List<Class<?>> singlePageAnnotationClasses = new ArrayList<Class<?>>(2);
        singlePageAnnotationClasses.add(PageDoc.class);
        singlePageAnnotationClasses.add(Page.class);
        for (Class<?> annotationClass : singlePageAnnotationClasses) {
            IAnnotationBinding annotation = getAnnotationBinding(annotations, annotationClass);
            if (annotation == null) {
                continue; // annotation is not found
            }
            if (allPageAnnotations.size() > 0) {
                // TODO throw IllegalTestScriptException
                throw new RuntimeException("don't use multiple page annoations at the same place");
            }
            allPageAnnotations.add(annotation);
        }

        List<Class<?>> multiplePageAnnotationClasses = new ArrayList<Class<?>>(2);
        multiplePageAnnotationClasses.add(PageDocs.class);
        multiplePageAnnotationClasses.add(Pages.class);
        for (Class<?> annotationClass : multiplePageAnnotationClasses) {
            IAnnotationBinding annotation = getAnnotationBinding(annotations, annotationClass);
            if (annotation == null) {
                continue; // annotation is not found
            }
            if (allPageAnnotations.size() > 0) {
                // TODO throw IllegalTestScriptException
                throw new RuntimeException("don't use multiple page annoations at the same place");
            }
            // get @PageDoc or @Page from @PageDocs or @Pages
            Object value = getAnnotationValue(annotation, "value");
            Object[] values = (Object[]) value;
            for (Object element : values) {
                allPageAnnotations.add((IAnnotationBinding) element);
            }
        }

        // get resultPageMap
        Map<Locale, String> resultPageMap
        = new HashMap<Locale, String>(allPageAnnotations.size());
        for (IAnnotationBinding eachPageAnnotation : allPageAnnotations) {
            Object value = getAnnotationValue(eachPageAnnotation, "value");
            Locale locale = getAnnotationLocaleValue(eachPageAnnotation, "locale");
            resultPageMap.put(locale, (String) value);
        }

        return resultPageMap;
    }

    // first... value
    // second... captureStyle value.
    // return null and default CaptureStyle pair if no TestDoc is found
    private static Pair<String, CaptureStyle> getTestDoc(
            IAnnotationBinding[] annotations, AcceptableLocales locales) {
        Pair<Map<Locale, String>, CaptureStyle> allTestDocs = getAllTestDocs(annotations);
        Map<Locale, String> testDocMap = allTestDocs.getLeft();
        if (testDocMap.isEmpty()) {
            return Pair.of(null, CaptureStyle.getDefault()); // no @TestDoc found
        }

        String testDoc = null;
        for (Locale locale : locales.getLocales()) {
            String value = testDocMap.get(locale);
            if (value != null) {
                testDoc = value;
                break;
            }
        }
        if (testDoc == null) {
            // set empty string if no locale matched data is found
            return Pair.of("", allTestDocs.getRight());
        } else {
            return Pair.of(testDoc, allTestDocs.getRight());
        }
    }

    // return null if no Page found
    private static String getPageDoc(
            IAnnotationBinding[] annotations, AcceptableLocales locales) {
        Map<Locale, String> allPages = getAllPageDocs(annotations);
        if (allPages.isEmpty()) {
            return null; // no @Page found
        }

        for (Locale locale : locales.getLocales()) {
            String value = allPages.get(locale);
            if (value != null) {
                return value;
            }
        }
        // set empty string if no locale matched data is found
        return "";
    }

    // return null if not found
    public static String getPageDoc(ITypeBinding type, AcceptableLocales locales) {
        return getPageDoc(type.getAnnotations(), locales);
    }

    // return null if not found
    public static String getTestDoc(ITypeBinding type, AcceptableLocales locales) {
        Pair<String, CaptureStyle> pair = getTestDoc(type.getAnnotations(), locales);
        return pair.getLeft();
    }

    // return null pair if not found
    public static Pair<String, CaptureStyle> getTestDoc(
            IMethodBinding method, AcceptableLocales locales) {
        return getTestDoc(method.getAnnotations(), locales);
    }

    public static String getTestDoc(
            IVariableBinding variable, AcceptableLocales locales) {
        return getTestDoc(variable.getAnnotations(), locales).getLeft();
    }
}
