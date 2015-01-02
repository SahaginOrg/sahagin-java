package org.sahagin.runlib.external;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
public @interface TestDocs {
    TestDoc[] value();
    CaptureStyle capture() default CaptureStyle.THIS_LINE;
}
