package org.sahagin.runlib.srctreegen;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.sahagin.runlib.external.TestDoc;
import org.sahagin.runlib.external.TestDocs;

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
            String qName = annotation.getAnnotationType().getQualifiedName();
            assert qName != null;
            if (qName.equals(annotationClassName)) {
                return annotation;
            }
        }
        return null;
    }

    // Get the method annotation whose class equals to annotationClass.
    // Return null if specified name annotation is not found.
    public static IAnnotationBinding getAnnotationBinding(
            IAnnotationBinding[] annotations, Class<?> annotationClass) {
        if (annotationClass == null) {
            throw new NullPointerException();
        }
        return getAnnotationBinding(annotations, annotationClass.getCanonicalName());
    }

    // returns null if specified varName annotation is not found
    public static Object getAnnotationValue(IAnnotationBinding annotation, String varName) {
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
    public static String getEnumAnnotationFieldName(IAnnotationBinding annotation, String varName) {
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
    public static CaptureStyle getAnnotationCaptureStyleValue(
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
    public static Locale getAnnotationLocaleValue(
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
    
    // return empty list and null pair if no TestDoc is found
    private static Pair<Map<Locale, String>, CaptureStyle> getAllTestDocs(
            IAnnotationBinding[] annotations) {
        IAnnotationBinding testDocAnnotation = getAnnotationBinding(annotations, TestDoc.class);
        IAnnotationBinding testDocsAnnotation = getAnnotationBinding(annotations, TestDocs.class);
        if (testDocAnnotation != null && testDocsAnnotation != null) {
            // TODO throw IllegalTestScriptException
            throw new RuntimeException("don't use @TestDoc and @TestDocs at the same place");
        }

        // all testDoc value on @TestDoc and @TestDocs
        List<IAnnotationBinding> allTestDocAnnotations = new ArrayList<IAnnotationBinding>(2);
        CaptureStyle resultCaptureStyle = null;

        if (testDocAnnotation != null) {
            // get @TestDoc
            allTestDocAnnotations.add(testDocAnnotation);
            resultCaptureStyle = getAnnotationCaptureStyleValue(testDocAnnotation, "capture");
        } else if (testDocsAnnotation != null) {
            // get @TestDoc from @TestDocs
            Object value = getAnnotationValue(testDocsAnnotation, "value");
            IAnnotationBinding[] testDocsAnnotationValues = (IAnnotationBinding[]) value;
            allTestDocAnnotations.addAll(Arrays.asList(testDocsAnnotationValues));
            resultCaptureStyle = getAnnotationCaptureStyleValue(testDocsAnnotation, "capture");
            for (IAnnotationBinding eachTestDocAnnotation : testDocsAnnotationValues) {
                if (getEnumAnnotationFieldName(eachTestDocAnnotation, "capture") != null) {
                    // TODO throw IllegalTestScriptException
                    throw new RuntimeException(
                            "capture must be set on not @TestDoc but @TestDocs");
                }
            }
        }
        
        // get resultTestDocValues
        Map<Locale, String> resultTestDocMap 
        = new HashMap<Locale, String>(allTestDocAnnotations.size());
        for (IAnnotationBinding eachTestDocAnnotation : allTestDocAnnotations) {
            Object value = getAnnotationValue(eachTestDocAnnotation, "value");
            Locale locale = getAnnotationLocaleValue(eachTestDocAnnotation, "locale");
            resultTestDocMap.put(locale, (String) value);
        }

        return Pair.of(resultTestDocMap, resultCaptureStyle);
    }
    
    // first... value
    // second... captureStyle value.
    // return null pair if no TestDoc is found
    private static Pair<String, CaptureStyle> getTestDoc(IAnnotationBinding[] annotations) {
    	Pair<Map<Locale, String>, CaptureStyle> allTestDocs = getAllTestDocs(annotations);
    	Map<Locale, String> testDocMap = allTestDocs.getLeft();
        if (testDocMap.isEmpty()) {
            return Pair.of(null, null);
        }
        
    	String testDoc = null;
        List<Locale> locales = Locale.getAcceptableLocales();
        for (Locale locale : locales) {
        	String value = testDocMap.get(locale);
        	if (value != null) {
        		testDoc = value;
        		break;
        	}
        }
        if (testDoc == null) {
        	return Pair.of(null, null);
        } else {
        	return Pair.of(testDoc, allTestDocs.getRight());
        }
    }

    // return null if no Page found
    public static String getPageTestDoc(IAnnotationBinding[] annotations) {
        IAnnotationBinding annotation = getAnnotationBinding(
                annotations, Page.class);
        if (annotation == null) {
            return null;
        }
        Object value = getAnnotationValue(annotation, "value");
        return (String) value;
    }

    // return null if not found
    public static String getPageTestDoc(ITypeBinding type) {
        return getPageTestDoc(type.getAnnotations());
    }

    // return null if not found
    public static String getTestDoc(ITypeBinding type) {
        Pair<String, CaptureStyle> pair = getTestDoc(type.getAnnotations());
        return pair.getLeft();
    }

    // return null pair if not found
    public static Pair<String, CaptureStyle> getTestDoc(IMethodBinding method) {
        return getTestDoc(method.getAnnotations());
    }

    // method name with qualified class name
    public static String qualifiedMethodName(IMethodBinding method) {
        String methodName = method.getName();
        String className = method.getDeclaringClass().getQualifiedName();
        return className + "." + methodName;
    }

}
