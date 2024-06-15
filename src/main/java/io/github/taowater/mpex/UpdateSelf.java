package io.github.taowater.mpex;


import io.github.taowater.core.util.EmptyUtil;

/**
 * 自更新
 *
 * @author zhu56
 * @date 2023/09/04 22:33
 */
interface UpdateSelf<W, R> {


    /**
     * 自增
     *
     * @param condition 条件
     * @param column    字段
     * @param increment 自增值
     * @return {@link W}
     */
    <N extends Number> W incr(boolean condition, R column, N increment);

    /**
     * 自增
     *
     * @param column    字段
     * @param increment 自增值
     * @return {@link W}
     */
    default <N extends Number> W incr(R column, N increment) {
        return this.incr(true, column, increment);
    }

    /**
     * 不为空才自增
     *
     * @param column    字段
     * @param increment 自增值
     * @return {@link W}
     */
    default <N extends Number> W incrX(R column, N increment) {
        return this.incr(EmptyUtil.isNotEmpty(increment), column, increment);
    }

    /**
     * 自增1
     *
     * @param condition 条件
     * @param column    字段
     * @return {@link W}
     */
    default W incr(boolean condition, R column) {
        return incr(condition, column, 1);
    }

    /**
     * 自增1
     *
     * @param column 字段
     * @return {@link W}
     */
    default W incr(R column) {
        return incr(column, 1);
    }

    /**
     * 自减
     *
     * @param condition 条件
     * @param column    字段
     * @param decrement 自减值
     * @return {@link W}
     */
    <N extends Number> W decr(boolean condition, R column, N decrement);

    /**
     * 自减
     *
     * @param column    字段
     * @param decrement 自减值
     * @return {@link W}
     */
    default <N extends Number> W decr(R column, N decrement) {
        return decr(true, column, decrement);
    }

    /**
     * 不为空才自减
     *
     * @param column    字段
     * @param decrement 自减值
     * @return {@link W}
     */
    default <N extends Number> W decrX(R column, N decrement) {
        return this.decr(EmptyUtil.isNotEmpty(decrement), column, decrement);
    }

    /**
     * 自减1
     *
     * @param condition 条件
     * @param column    字段
     * @return {@link W}
     */
    default W decr(boolean condition, R column) {
        return decr(condition, column, 1);
    }

    /**
     * 自减1
     *
     * @param column 字段
     * @return {@link W}
     */
    default W decr(R column) {
        return decr(column, 1);
    }
}
