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
     * 忽视
     */
    IGNORE(o -> false),
    /**
     * 忽略空
     */
    IGNORE_EMPTY(EmptyUtil::isNotEmpty),
    /**
     * 所有
     */
    ALLWAYS(o -> true),
    /**
     * 如果为空，则返回空
     */
    RETURN_EMPTY_IF_EMPTY(EmptyUtil::isNotEmpty),
    ;

    /**
     * 策略判断是否组装的方法
     */
    private final Predicate<Object> predicate;


}
