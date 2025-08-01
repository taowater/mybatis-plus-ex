package com.taowater.mpx.wrapper.interfaces;

import com.baomidou.mybatisplus.extension.conditions.query.ChainQuery;

/**
 * 链式查询拓展
 */
public interface ChainQueryEx<T> extends ChainQuery<T> {

    /**
     * 获取单个
     */
    @Override
    default T one() {
        return one(false);
    }

    /**
     * 获取单个
     *
     * @param throwEx 若存在多条是否抛出异常
     */
    default T one(boolean throwEx) {
        return execute(mapper -> mapper.selectOne(getWrapper(), throwEx));
    }

    /**
     * 判断数据是否存在
     *
     * @return true 存在 false 不存在
     */
    @Override
    default boolean exists() {
        return execute(mapper -> mapper.exists(getWrapper()));
    }

}
