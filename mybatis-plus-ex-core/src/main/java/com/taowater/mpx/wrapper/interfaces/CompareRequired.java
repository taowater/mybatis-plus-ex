package com.taowater.mpx.wrapper.interfaces;

import com.baomidou.mybatisplus.core.conditions.interfaces.Compare;
import com.baomidou.mybatisplus.core.conditions.interfaces.Func;
import com.taowater.taol.core.util.EmptyUtil;

import java.util.Collection;

/**
 * 必需参数组装
 * 如果对应参数为空则截断查询结果返空
 * 原有方法名后加R
 *
 * @author zhu56
 */
@SuppressWarnings("unused")
public interface CompareRequired<W, R> extends Compare<W, R>, Func<W, R> {

    void setExecute(boolean flag);

    default void calcExecute(Object value) {
        setExecute(needExecute() && EmptyUtil.isNotEmpty(value));
    }

    /**
     * 是否需要执行
     *
     * @return boolean
     */
    boolean needExecute();

    default W inR(R column, Collection<?> coll) {
        calcExecute(coll);
        return in(column, coll);
    }

    default W inR(R column, Object... values) {
        calcExecute(values);
        return in(column, values);
    }

    default W notInR(R column, Collection<?> coll) {
        calcExecute(coll);
        return notIn(column, coll);
    }

    default W notInR(R column, Object... values) {
        calcExecute(values);
        return notIn(column, values);
    }

    default W eqR(R column, Object val) {
        calcExecute(val);
        return eq(column, val);
    }

    default W neR(R column, Object val) {
        calcExecute(val);
        return ne(column, val);
    }

    default W gtR(R column, Object val) {
        calcExecute(val);
        return gt(column, val);
    }

    default W geR(R column, Object val) {
        calcExecute(val);
        return ge(column, val);
    }

    default W ltR(R column, Object val) {
        calcExecute(val);
        return lt(column, val);
    }

    default W leR(R column, Object val) {
        calcExecute(val);
        return le(column, val);
    }

    default W betweenR(R column, Object val1, Object val2) {
        calcExecute(val1);
        calcExecute(val2);
        return between(column, val1, val2);
    }

    default W notBetweenR(R column, Object val1, Object val2) {
        calcExecute(val1);
        calcExecute(val2);
        return notBetween(column, val1, val2);
    }

    default W likeR(R column, Object val) {
        calcExecute(val);
        return like(column, val);
    }

    default W notLikeR(R column, Object val) {
        calcExecute(val);
        return notLike(column, val);
    }

    default W likeLeftR(R column, Object val) {
        calcExecute(val);
        return likeLeft(column, val);
    }

    default W likeRightR(R column, Object val) {
        calcExecute(val);
        return likeRight(column, val);
    }
}
