package com.taowater.mpx.filter.op;

import com.taowater.mpx.filter.AliasFor;
import com.taowater.mpx.filter.FilterStrategy;
import com.taowater.mpx.filter.Operator;

import java.lang.annotation.*;

/**
 * not_in操作
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Filter(operate = Operator.NOT_IN)
public @interface NotIn {
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