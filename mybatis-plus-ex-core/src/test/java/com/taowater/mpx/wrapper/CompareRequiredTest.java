package com.taowater.mpx.wrapper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code *R}（必需参数）系列的短路语义：
 * 只要有一个必需参数为空，整个 wrapper 就应标记为不执行，且该状态具有"粘性"——
 * 后续再传入非空的必需参数也不应把它重新打开。
 */
class CompareRequiredTest {

    @Test
    void allRequiredPresent_shouldExecute() {
        QueryExWrapper<Object> w = new QueryExWrapper<>();
        w.eqR("name", "tao").gtR("age", 18);

        assertThat(w.needExecute()).isTrue();
    }

    @Test
    void oneRequiredEmpty_shouldNotExecute() {
        QueryExWrapper<Object> w = new QueryExWrapper<>();
        w.eqR("name", null);

        assertThat(w.needExecute()).isFalse();
    }

    @Test
    void emptyIsSticky_laterNonEmptyDoesNotReopen() {
        QueryExWrapper<Object> w = new QueryExWrapper<>();
        // 先空 -> 关闭；再非空也不应重新打开
        w.eqR("name", null).gtR("age", 18);

        assertThat(w.needExecute()).isFalse();
    }

    @Test
    void gtR_emptyValue_shouldNotExecute() {
        QueryExWrapper<Object> w = new QueryExWrapper<>();
        w.gtR("age", null);

        assertThat(w.needExecute()).isFalse();
    }

    @Test
    void betweenR_anyBoundEmpty_shouldNotExecute() {
        QueryExWrapper<Object> w = new QueryExWrapper<>();
        w.betweenR("age", 10, null);

        assertThat(w.needExecute()).isFalse();
    }

    @Test
    void inR_emptyCollection_shouldNotExecute() {
        QueryExWrapper<Object> w = new QueryExWrapper<>();
        w.inR("id", java.util.Collections.emptyList());

        assertThat(w.needExecute()).isFalse();
    }
}
