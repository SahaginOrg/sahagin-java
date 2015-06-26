package org.sahagin.runlib.external;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
public @interface PageDoc {
    String value();
    Locale locale() default Locale.DEFAULT;
}
