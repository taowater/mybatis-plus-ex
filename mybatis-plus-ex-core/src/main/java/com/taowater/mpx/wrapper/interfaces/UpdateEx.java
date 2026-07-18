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
     * 校验自操作运算符，仅允许基本算术运算符，防止 SQL 注入。
     *
     * @param keyword 运算符
     * @return 规范化后的运算符
     */
    static String safeKeyword(String keyword) {
        if (keyword != null) {
            switch (keyword.trim()) {
                case "+":
                case "-":
                case "*":
                case "/":
                    return keyword.trim();
                default:
                    break;
            }
        }
        throw new IllegalArgumentException("Illegal self keyword: " + keyword + " (allowed: + - * /)");
    }

    /**
     * 自增（MP 3.5+ {@link #setIncrBy} 实现，经 {@link #self} 参数绑定）
     */
    @Override
    default W setIncrBy(boolean condition, R column, Number val) {
        return self(condition, column, Constants.PLUS, val);
    }

    /**
     * 自增1
     *
     * @param condition 条件
     * @param column    字段
     * @return {@link W}
     */
    default W setIncrBy(boolean condition, R column) {
        return setIncrBy(condition, column, 1);
    }

    /**
     * 自增1
     *
     * @param column 字段
     * @return {@link W}
     */
    default W setIncrBy(R column) {
        return setIncrBy(column, 1);
    }

    /**
     * 自减（MP 3.5+ {@link #setDecrBy} 实现，经 {@link #self} 参数绑定）
     */
    @Override
    default W setDecrBy(boolean condition, R column, Number val) {
        return self(condition, column, Constants.DASH, val);
    }

    /**
     * 自减1
     *
     * @param condition 条件
     * @param column    字段
     * @return {@link W}
     */
    default W setDecrBy(boolean condition, R column) {
        return setDecrBy(condition, column, 1);
    }

    /**
     * 自减1
     *
     * @param column 字段
     * @return {@link W}
     */
    default W setDecrBy(R column) {
        return setDecrBy(column, 1);
    }

    /**
     * 不为空才自增
     *
     * @param column    字段
     * @param increment 自增值
     * @return {@link W}
     */
    default <N extends Number> W setIncrByX(R column, N increment) {
        return setIncrBy(EmptyUtil.isNotEmpty(increment), column, increment);
    }

    /**
     * 不为空才自减
     *
     * @param column    字段
     * @param decrement 自减值
     * @return {@link W}
     */
    default <N extends Number> W setDecrByX(R column, N decrement) {
        return setDecrBy(EmptyUtil.isNotEmpty(decrement), column, decrement);
    }
}
