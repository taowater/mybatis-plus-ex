package com.taowater.mpx.wrapper.chain;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.taowater.mpx.wrapper.LambdaQueryExWrapper;
import com.taowater.mpx.wrapper.interfaces.ChainQueryEx;
import com.taowater.mpx.wrapper.interfaces.QueryEx;

import java.util.List;
import java.util.function.Predicate;

/**
 * 拓展lambda链式查询wrapper
 *
 * @see LambdaQueryChainWrapper
 */
public class LambdaQueryChainExWrapper<T> extends AbstractChainExWrapper<T, SFunction<T, ?>, LambdaQueryChainExWrapper<T>, LambdaQueryExWrapper<T>>
        implements ChainQueryEx<T>, QueryEx<LambdaQueryChainExWrapper<T>, T, SFunction<T, ?>> {

    public LambdaQueryChainExWrapper(BaseMapper<T> baseMapper) {
        super(baseMapper);
        super.wrapperChildren = new LambdaQueryExWrapper<>();
    }

    public LambdaQueryChainExWrapper(Class<T> entityClass) {
        super(null);
        super.wrapperChildren = new LambdaQueryExWrapper<>(entityClass);
    }

    public LambdaQueryChainExWrapper(BaseMapper<T> baseMapper, T entity) {
        super(baseMapper);
        super.wrapperChildren = new LambdaQueryExWrapper<>(entity);
    }

    public LambdaQueryChainExWrapper(BaseMapper<T> baseMapper, Class<T> entityClass) {
        super(baseMapper);
        super.wrapperChildren = new LambdaQueryExWrapper<>(entityClass);
    }

    public LambdaQueryChainExWrapper(BaseMapper<T> baseMapper, LambdaQueryExWrapper<T> wrapperChildren) {
        super(baseMapper);
        super.wrapperChildren = wrapperChildren;
    }

    @Override
    public void setLimit(Integer limit) {
        wrapperChildren.setLimit(limit);
    }


    @Override
    public LambdaQueryChainExWrapper<T> select(boolean condition, List<SFunction<T, ?>> columns) {
        return doSelect(condition, columns);
    }

    @Override
    @SafeVarargs
    public final LambdaQueryChainExWrapper<T> select(SFunction<T, ?>... columns) {
        return doSelect(true, CollectionUtils.toList(columns));
    }

    @Override
    @SafeVarargs
    public final LambdaQueryChainExWrapper<T> select(boolean condition, SFunction<T, ?>... columns) {
        return doSelect(condition, CollectionUtils.toList(columns));
    }

    @Override
    public LambdaQueryChainExWrapper<T> select(Class<T> entityClass, Predicate<TableFieldInfo> predicate) {
        wrapperChildren.select(entityClass, predicate);
        return typedThis;
    }

    protected LambdaQueryChainExWrapper<T> doSelect(boolean condition, List<SFunction<T, ?>> columns) {
        wrapperChildren.select(condition, columns);
        return typedThis;
    }
}
