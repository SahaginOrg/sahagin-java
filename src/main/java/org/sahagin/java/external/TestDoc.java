package org.sahagin.java.external;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

// TODO enum and constant variable support
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
public @interface TestDoc {
    String value();
    boolean stepInCapture() default false;
}
