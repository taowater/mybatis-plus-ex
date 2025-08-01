package com.taowater.mpx.wrapper.chain;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.extension.conditions.AbstractChainWrapper;
import com.taowater.mpx.wrapper.interfaces.CompareEx;
import com.taowater.mpx.wrapper.interfaces.CompareRequired;

/**
 * 抽象链式wrapper
 *
 * @see AbstractChainWrapper
 */
public abstract class AbstractChainExWrapper<T, R, Children extends AbstractChainExWrapper<T, R, Children, Param>, Param extends AbstractWrapper<T, R, Param> & CompareRequired<Param, R>>
        extends AbstractChainWrapper<T, R, Children, Param> implements CompareEx<Children, R> {


    @Override
    public boolean needExecute() {
        return wrapperChildren.needExecute();
    }

    @Override
    public void setExecute(boolean flag) {
        wrapperChildren.setExecute(flag);
    }

    public AbstractWrapper<T, R, Param> getWrapper() {
        return wrapperChildren;
    }

    public Class<T> getEntityClass() {
        return super.wrapperChildren.getEntityClass();
    }


}
