package com.taowater.mpx.filter.op;

import com.taowater.mpx.filter.FilterStrategy;
import com.taowater.mpx.filter.Operator;

import java.lang.annotation.*;

/**
 * 大于等于操作
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Filter(operate = Operator.GE)
public @interface Ge {
    /**
     * 目标字段（空串表示使用参数属性名）
     */
    String field() default "";

    /**
     * 过滤策略
     */
    FilterStrategy filter() default FilterStrategy.IGNORE_EMPTY;
}
