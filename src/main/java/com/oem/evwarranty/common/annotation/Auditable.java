package com.oem.evwarranty.common.annotation;

import java.lang.annotation.*;

/**
 * Annotation for marking service methods to automatically log audit trail entries via AOP.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {
    String action();
    String resourceType();
}
