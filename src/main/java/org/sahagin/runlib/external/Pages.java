package org.sahagin.runlib.external;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

// alias of PageDocs
/**
 * @deprecated use {@link PageDocs} instead.
 */
@Target({ElementType.TYPE})
public @interface Pages {
    Page[] value();
}
