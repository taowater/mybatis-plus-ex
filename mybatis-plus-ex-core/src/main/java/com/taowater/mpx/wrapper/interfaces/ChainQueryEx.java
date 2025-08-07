package com.taowater.mpx.wrapper.interfaces;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.ChainQuery;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.taowater.mpx.mapper.BaseMapper;

import java.util.List;

/**
 * 链式查询拓展
 */
public interface ChainQueryEx<T> extends ChainQuery<T> {

    /**
     * 获取 BaseMapper
     */
    @Override
    BaseMapper<T> getBaseMapper();

    /**
     * 获取单个
     */
    @Override
    default T one() {
        return one(false);
    }

    default <D> D one(Class<D> returnType) {
        return one(returnType, false);
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
     * 获取单个
     *
     * @param throwEx 若存在多条是否抛出异常
     */
    default <D> D one(Class<D> returnType, boolean throwEx) {
        return exe(mapper -> mapper.selectOne(returnType, getWrapper(), throwEx));
    }

    /**
     * 获取集合
     *
     * @param returnType 返回类型
     */
    default <D> List<D> list(Class<D> returnType) {
        return exe(mapper -> mapper.selectList(returnType, getWrapper()));
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

    default <R> R exe(SFunction<BaseMapper<T>, R> function) {
        BaseMapper<T> baseMapper = getBaseMapper();
        if (baseMapper != null) {
            return function.apply(baseMapper);
        }
        return SqlHelper.execute(getEntityClass(), function);
    }

}
