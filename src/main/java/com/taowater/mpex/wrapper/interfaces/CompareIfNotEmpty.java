package com.taowater.mpex.wrapper.interfaces;

import com.baomidou.mybatisplus.core.conditions.interfaces.Compare;
import com.baomidou.mybatisplus.core.conditions.interfaces.Func;
import com.taowater.taol.core.util.EmptyUtil;

import java.util.Collection;

/**
 * 不为空才组装
 * 原有方法名后加X
 *
 * @author zhu56
 * @date 2023/09/02 01:38
 */
public interface CompareIfNotEmpty<W, R> extends Compare<W, R>, Func<W, R> {

    default W inX(R column, Collection<?> coll) {
        return in(EmptyUtil.isNotEmpty(coll), column, coll);
    }

    default W inX(R column, Object... values) {
        return in(EmptyUtil.isNotEmpty(values), column, values);
    }

    default W notInX(R column, Collection<?> coll) {
        return notIn(EmptyUtil.isNotEmpty(coll), column, coll);
    }

    default W notInX(R column, Object... values) {
        return notIn(EmptyUtil.isNotEmpty(values), column, values);
    }

    default W eqX(R column, Object val) {
        return this.eq(EmptyUtil.isNotEmpty(val), column, val);
    }

    default W neX(R column, Object val) {
        return this.ne(EmptyUtil.isNotEmpty(val), column, val);
    }

    default W gtX(R column, Object val) {
        return this.gt(EmptyUtil.isNotEmpty(val), column, val);
    }

    default W geX(R column, Object val) {
        return this.ge(EmptyUtil.isNotEmpty(val), column, val);
    }

    default W ltX(R column, Object val) {
        return this.lt(EmptyUtil.isNotEmpty(val), column, val);
    }

    default W leX(R column, Object val) {
        return le(EmptyUtil.isNotEmpty(val), column, val);
    }

    default W betweenX(R column, Object val1, Object val2) {
        return between(EmptyUtil.isAllNotEmpty(val1, val2), column, val1, val2);
    }

    default W notBetweenX(R column, Object val1, Object val2) {
        return notBetween(EmptyUtil.isAllNotEmpty(val1, val2), column, val1, val2);
    }

    default W likeX(R column, Object val) {
        return like(EmptyUtil.isNotEmpty(val), column, val);
    }

    default W notLikeX(R column, Object val) {
        return notLike(EmptyUtil.isNotEmpty(val), column, val);
    }

    default W likeLeftX(R column, Object val) {
        return likeLeft(EmptyUtil.isNotEmpty(val), column, val);
    }

    default W likeRightX(R column, Object val) {
        return likeRight(EmptyUtil.isNotEmpty(val), column, val);
    }
}
