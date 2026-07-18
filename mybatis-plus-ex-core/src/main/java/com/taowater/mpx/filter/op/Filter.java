package com.taowater.mpx.filter.op;

import com.taowater.mpx.filter.FilterStrategy;
import com.taowater.mpx.filter.Operator;

import java.lang.annotation.*;

/**
 * 过滤参数
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Filter {
    /**
     * 目标字段
     */
    String field() default "";

    /**
     * 过滤策略
     */
    FilterStrategy filter() default FilterStrategy.IGNORE_EMPTY;

    /**
     * 操作类型
     */
    Operator operate() default Operator.EQ;
}