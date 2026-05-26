package com.taowater.mpx.filter;

import java.lang.annotation.*;

/**
 * 注解别名
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AliasFor {
    Class<? extends Annotation> annotation() default Annotation.class;

    String value();
}
