package org.sahagin.runlib.external;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

// TODO enum and constant variable support
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
public @interface TestDoc {
    String value();
    CaptureStyle capture() default CaptureStyle.THIS_LINE;
}
