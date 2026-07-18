package com.taowater.mpx.wrapper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * limit 数值校验：{@code ${ew.limit}} 直接拼入 SQL，仅允许非负整数，负数必须被拒绝。
 */
class QueryLimitTest {

    @Test
    void limit_negative_rejectedOnQueryWrapper() {
        assertThatThrownBy(() -> new QueryExWrapper<>().limit(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void setLimit_negative_rejectedOnQueryWrapper() {
        assertThatThrownBy(() -> new QueryExWrapper<>().setLimit(-5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void limit_negative_rejectedOnLambdaWrapper() {
        assertThatThrownBy(() -> new LambdaQueryExWrapper<>().limit(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void limit_zeroAndPositive_accepted() {
        QueryExWrapper<Object> w = new QueryExWrapper<>();
        w.limit(0);
        assertThat(w.getLimit()).isZero();

        w.limit(10);
        assertThat(w.getLimit()).isEqualTo(10);
    }

    @Test
    void setLimit_null_meansNoLimit() {
        QueryExWrapper<Object> w = new QueryExWrapper<>();
        w.setLimit(null);
        assertThat(w.getLimit()).isNull();
    }
}
