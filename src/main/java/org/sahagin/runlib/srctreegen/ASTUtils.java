package org.sahagin.runlib.srctreegen;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
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
                return value.getValue();
            }
        }
        return null;
    }

    // first... value
    // second... stepInCapture flag.
    // return null if no TestDoc found
    public static Pair<String, Boolean> getTestDoc(IAnnotationBinding[] annotations) {
        IAnnotationBinding annotation = ASTUtils.getAnnotationBinding(
                annotations, TestDoc.class);
        if (annotation == null) {
            return null;
        }
        Object value = ASTUtils.getAnnotationValue(annotation, "value");
        Object stepInCaptureObj = ASTUtils.getAnnotationValue(annotation, "stepInCapture");
        boolean stepInCapture;
        if (stepInCaptureObj == null) {
            stepInCapture = false; // default value
        } else {
            stepInCapture = (Boolean) stepInCaptureObj;
        }
        return Pair.of((String) value, stepInCapture);
    }

    // return null if no Page found
    public static String getPageTestDoc(IAnnotationBinding[] annotations) {
        IAnnotationBinding annotation = ASTUtils.getAnnotationBinding(
                annotations, Page.class);
        if (annotation == null) {
            return null;
        }
        Object value = ASTUtils.getAnnotationValue(annotation, "value");
        return (String) value;
    }

    // return null if not found
    public static String getPageTestDoc(ITypeBinding type) {
        return getPageTestDoc(type.getAnnotations());
    }

    // return null if not found
    public static String getTestDoc(ITypeBinding type) {
        Pair<String, Boolean> pair = getTestDoc(type.getAnnotations());
        if (pair != null) {
            return pair.getLeft();
        } else {
            return null;
        }
    }

    // return null if not found
    public static Pair<String, Boolean> getTestDoc(IMethodBinding method) {
        return getTestDoc(method.getAnnotations());
    }

    // method name with qualified class name
    public static String qualifiedMethodName(IMethodBinding method) {
        String methodName = method.getName();
        String className = method.getDeclaringClass().getQualifiedName();
        return className + "." + methodName;
    }

}
