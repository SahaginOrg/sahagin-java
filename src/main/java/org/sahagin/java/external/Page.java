package org.sahagin.java.external;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
public @interface Page {
    String value();
}
