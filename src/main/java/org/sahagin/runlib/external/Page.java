package org.sahagin.runlib.external;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

// alias of PageDoc
/**
 * @deprecated use {@link PageDoc} instead.
 */
@Target({ElementType.TYPE})
public @interface Page {
    String value();
    Locale locale() default Locale.DEFAULT;
}
