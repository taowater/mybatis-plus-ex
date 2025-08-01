package com.taowater.mpx.wrapper.chain;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.AbstractChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.ChainUpdate;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.taowater.mpx.wrapper.LambdaUpdateExWrapper;
import com.taowater.mpx.wrapper.interfaces.UpdateEx;

/**
 * 拓展lambda链式更新wrapper
 *
 * @see LambdaUpdateChainWrapper
 */
public class LambdaUpdateChainExWrapper<T> extends AbstractChainWrapper<T, SFunction<T, ?>, LambdaUpdateChainExWrapper<T>, LambdaUpdateExWrapper<T>>
        implements ChainUpdate<T>, UpdateEx<LambdaUpdateChainExWrapper<T>, SFunction<T, ?>> {

    private final BaseMapper<T> baseMapper;

    public LambdaUpdateChainExWrapper(BaseMapper<T> baseMapper) {
        super();
        this.baseMapper = baseMapper;
        super.wrapperChildren = new LambdaUpdateExWrapper<>();
    }

    public LambdaUpdateChainExWrapper(Class<T> entityClass) {
        super();
        this.baseMapper = null;
        super.wrapperChildren = new LambdaUpdateExWrapper<>(entityClass);
    }

    public LambdaUpdateChainExWrapper(BaseMapper<T> baseMapper, LambdaUpdateExWrapper<T> wrapperChildren) {
        super();
        this.baseMapper = baseMapper;
        super.wrapperChildren = wrapperChildren;
    }

    @Override
    public LambdaUpdateChainExWrapper<T> set(boolean condition, SFunction<T, ?> column, Object val, String mapping) {
        wrapperChildren.set(condition, column, val, mapping);
        return typedThis;
    }

    @Override
    public LambdaUpdateChainExWrapper<T> setSql(boolean condition, String setSql, Object... params) {
        wrapperChildren.setSql(condition, setSql, params);
        return typedThis;
    }

    @Override
    public BaseMapper<T> getBaseMapper() {
        return baseMapper;
    }

    @Override
    public Class<T> getEntityClass() {
        return super.wrapperChildren.getEntityClass();
    }

    @Override
    public LambdaUpdateChainExWrapper<T> self(boolean condition, SFunction<T, ?> column, String keyword, Object val) {
        wrapperChildren.self(condition, column, keyword, val);
        return typedThis;
    }
}
