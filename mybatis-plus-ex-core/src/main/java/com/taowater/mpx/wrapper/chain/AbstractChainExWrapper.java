package com.taowater.mpx.wrapper.chain;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.baomidou.mybatisplus.extension.conditions.AbstractChainWrapper;
import com.taowater.mpx.mapper.BaseMapper;
import com.taowater.mpx.wrapper.interfaces.CompareEx;

/**
 * 抽象链式wrapper
 *
 * @see AbstractChainWrapper
 */
public abstract class AbstractChainExWrapper<T, R, Children extends AbstractChainExWrapper<T, R, Children, Param>, Param extends AbstractWrapper<T, R, Param> & CompareEx<Param, R>>
        extends AbstractChainWrapper<T, R, Children, Param> implements CompareEx<Children, R> {

    private final BaseMapper<T> baseMapper;

    protected AbstractChainExWrapper(BaseMapper<T> baseMapper) {
        this.baseMapper = baseMapper;
    }

    @Override
    public boolean needExecute() {
        return wrapperChildren.needExecute();
    }

    @Override
    public void setExecute(boolean flag) {
        wrapperChildren.setExecute(flag);
    }

    public Class<T> getEntityClass() {
        return super.wrapperChildren.getEntityClass();
    }

    public BaseMapper<T> getBaseMapper() {
        return baseMapper;
    }

    @Override
    public Children addConditionCol(boolean condition, R column1, SqlKeyword sqlKeyword, R column2) {
        wrapperChildren.addConditionCol(condition, column1, sqlKeyword, column2);
        return typedThis;
    }
}
