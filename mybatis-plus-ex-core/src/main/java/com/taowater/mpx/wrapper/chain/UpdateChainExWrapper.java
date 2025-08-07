package com.taowater.mpx.wrapper.chain;

import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.extension.conditions.update.ChainUpdate;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.taowater.mpx.mapper.BaseMapper;
import com.taowater.mpx.wrapper.UpdateExWrapper;
import com.taowater.mpx.wrapper.interfaces.UpdateEx;

/**
 * 拓展更新链式wrapper
 *
 * @see UpdateChainWrapper
 */
public class UpdateChainExWrapper<T> extends AbstractChainExWrapper<T, String, UpdateChainExWrapper<T>, UpdateExWrapper<T>>
        implements ChainUpdate<T>, UpdateEx<UpdateChainExWrapper<T>, String> {

    public UpdateChainExWrapper(BaseMapper<T> baseMapper) {
        super(baseMapper);
        super.wrapperChildren = new UpdateExWrapper<>();
    }

    public UpdateChainExWrapper(Class<T> entityClass) {
        super(null);
        super.wrapperChildren = new UpdateExWrapper<>();
        super.setEntityClass(entityClass);
    }

    @Override
    public UpdateChainExWrapper<T> set(boolean condition, String column, Object val, String mapping) {
        wrapperChildren.set(condition, column, val, mapping);
        return typedThis;
    }

    @Override
    public UpdateChainExWrapper<T> setSql(boolean condition, String setSql, Object... params) {
        wrapperChildren.setSql(condition, setSql, params);
        return typedThis;
    }

    @Override
    public UpdateChainExWrapper<T> self(boolean condition, String column, String keyword, Object val) {
        wrapperChildren.self(condition, column, keyword, val);
        return typedThis;
    }

    @Override
    public String getSqlSet() {
        throw ExceptionUtils.mpe("can not use this method for \"%s\"", "getSqlSet");
    }

    public LambdaUpdateChainExWrapper<T> lambda() {
        return new LambdaUpdateChainExWrapper<>(
                getBaseMapper(),
                wrapperChildren.lambda()
        );
    }
}
