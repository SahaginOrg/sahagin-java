package org.sahagin.runlib.srctreegen;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.sahagin.runlib.external.CaptureStyle;
import org.sahagin.runlib.external.Page;
import org.sahagin.runlib.external.TestDoc;

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


    // first... value
    // second... captureStyle value.
    // return null if no TestDoc found
    public static Pair<String, CaptureStyle> getTestDoc(IAnnotationBinding[] annotations) {
        IAnnotationBinding annotation = getAnnotationBinding(
                annotations, TestDoc.class);
        if (annotation == null) {
            return null;
        }
        Object value = getAnnotationValue(annotation, "value");
        String fieldName = getEnumAnnotationFieldName(annotation, "capture");
        CaptureStyle captureStyle;
        if (fieldName == null) {
            captureStyle = CaptureStyle.THIS_LINE; // default value
        } else {
            captureStyle = CaptureStyle.valueOf(fieldName);
            if (captureStyle == null) {
                throw new RuntimeException("invalid captureStyle: " + fieldName);
            }
        }
        return Pair.of((String) value, captureStyle);
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
        if (pair != null) {
            return pair.getLeft();
        } else {
            return null;
        }
    }

    // return null if not found
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
