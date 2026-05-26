package com.taowater.mpx.filter.op;

import com.taowater.mpx.filter.AliasFor;
import com.taowater.mpx.filter.FilterStrategy;
import com.taowater.mpx.filter.Operator;

import java.lang.annotation.*;

/**
 * 大于操作
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Filter(operate = Operator.GT)
public @interface Gt {
    /**
     * 目标字段
     */
    @AliasFor(annotation = Filter.class, value = "field")
    String field() default "";

    /**
     * 过滤策略
     */
    @AliasFor(annotation = Filter.class, value = "filter")
    FilterStrategy filter() default FilterStrategy.IGNORE_EMPTY;
}