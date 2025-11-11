package com.taowater.mpx.filter;

/**
 * 过滤策略
 */
public enum FilterStrategy {

    /**
     * 忽视
     */
    IGNORE,
    /**
     * 忽略空
     */
    IGNORE_EMPTY,
    /**
     * 所有
     */
    ALLWAYS,
    /**
     * 如果为空，则返回空
     */
    RETURN_EMPTY_IF_EMPTY

}
