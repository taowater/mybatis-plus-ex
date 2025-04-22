package com.taowater.mpex.wrapper.interfaces;

import com.baomidou.mybatisplus.core.conditions.query.Query;

/**
 * 查询拓展
 *
 * @author zhu56
 * @date 2025/04/22 21:16
 */
public interface QueryEx<Children, T, R> extends Query<Children, T, R> {

    void setLimit(Integer limit);

    /**
     * @param limit 限制条数
     */
    @SuppressWarnings("UnusedReturnValue")
    default Children limit(int limit) {
        setLimit(limit);
        return (Children) this;
    }
}
