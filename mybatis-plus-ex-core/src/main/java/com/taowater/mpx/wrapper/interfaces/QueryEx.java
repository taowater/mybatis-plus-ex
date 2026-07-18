package com.taowater.mpx.wrapper.interfaces;

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
     * @param limit 限制条数（须 &gt;= 0；经 {@code ${ew.limit}} 拼入 SQL，仅允许非负整数）
     */
    @SuppressWarnings("UnusedReturnValue")
    default Children limit(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("limit must be >= 0, got: " + limit);
        }
        setLimit(limit);
        return (Children) this;
    }
}
