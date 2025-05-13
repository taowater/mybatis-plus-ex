package com.taowater.mpx.wrapper.interfaces;


import com.baomidou.mybatisplus.core.conditions.update.Update;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.taowater.taol.core.util.EmptyUtil;

/**
 * 更新操作-包含自更新操作
 *
 * @author zhu56
 */
@SuppressWarnings("unused")
public interface UpdateEx<W, R> extends Update<W, R> {

    /**
     * 字段自操作
     *
     * @param condition 条件
     * @param column    字段
     * @param keyword   关键字
     * @param val       值
     */
    W self(boolean condition, R column, String keyword, Object val);

    /**
     * 自增
     *
     * @param condition 条件
     * @param column    字段
     * @param increment 自增值
     * @return {@link W}
     */
    default <N extends Number> W incr(boolean condition, R column, N increment) {
        return self(condition, column, Constants.PLUS, increment);
    }

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
    default <N extends Number> W decr(boolean condition, R column, N decrement) {
        return self(condition, column, Constants.DASH, decrement);
    }

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
        return decr(EmptyUtil.isNotEmpty(decrement), column, decrement);
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


    @Override
    default W setIncrBy(boolean condition, R column, Number val) {
        return incr(condition, column, val);
    }

    @Override
    default W setDecrBy(boolean condition, R column, Number val) {
        return decr(condition, column, val);
    }
}
