package com.taowater.mpx.filter;

import com.taowater.taol.core.util.EmptyUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Predicate;

/**
 * 过滤策略
 */
@Getter
@AllArgsConstructor
public enum FilterStrategy {

    /**
     * 忽略：无论是否有值都不参与条件
     */
    IGNORE(o -> false),
    /**
     * 忽略空值：仅当值非空时才参与条件
     */
    IGNORE_EMPTY(EmptyUtil::isNotEmpty),
    /**
     * 总是参与条件（含空值）
     */
    ALWAYS(o -> true),
    /**
     * 值为空时短路，整个查询返回空结果
     */
    RETURN_EMPTY_IF_EMPTY(EmptyUtil::isNotEmpty),
    ;

    /**
     * 策略判断是否组装的方法
     */
    private final Predicate<Object> predicate;

}
