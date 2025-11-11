package com.taowater.mpx.filter;

import java.lang.annotation.*;

/**
 * 等于操作
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Filter(operate = Operator.EQ)
public @interface Eq {
    /**
     * 目标字段
     */
    String field() default "";

    /**
     * 过滤策略
     */
    FilterStrategy filter() default FilterStrategy.IGNORE_EMPTY;
}