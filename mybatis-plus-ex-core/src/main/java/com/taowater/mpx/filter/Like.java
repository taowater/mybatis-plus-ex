package com.taowater.mpx.filter;

import java.lang.annotation.*;

/**
 * like操作
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Filter(operate = Operator.LIKE)
public @interface Like {
    /**
     * 目标字段
     */
    String field() default "";

    /**
     * 过滤策略
     */
    FilterStrategy filter() default FilterStrategy.IGNORE_EMPTY;
}