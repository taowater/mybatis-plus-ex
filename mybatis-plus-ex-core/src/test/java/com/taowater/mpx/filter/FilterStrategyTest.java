package com.taowater.mpx.filter;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link FilterStrategy} 各策略 predicate 语义。
 */
class FilterStrategyTest {

    @Test
    void ignore_neverApplies() {
        assertThat(FilterStrategy.IGNORE.getPredicate().test("x")).isFalse();
        assertThat(FilterStrategy.IGNORE.getPredicate().test(null)).isFalse();
    }

    @Test
    void ignoreEmpty_appliesOnlyWhenNotEmpty() {
        assertThat(FilterStrategy.IGNORE_EMPTY.getPredicate().test("x")).isTrue();
        assertThat(FilterStrategy.IGNORE_EMPTY.getPredicate().test(null)).isFalse();
        assertThat(FilterStrategy.IGNORE_EMPTY.getPredicate().test("")).isFalse();
        assertThat(FilterStrategy.IGNORE_EMPTY.getPredicate().test(Collections.emptyList())).isFalse();
    }

    @Test
    void always_appliesEvenWhenEmpty() {
        assertThat(FilterStrategy.ALWAYS.getPredicate().test(null)).isTrue();
        assertThat(FilterStrategy.ALWAYS.getPredicate().test("x")).isTrue();
    }

    @Test
    void returnEmptyIfEmpty_appliesOnlyWhenNotEmpty() {
        assertThat(FilterStrategy.RETURN_EMPTY_IF_EMPTY.getPredicate().test("x")).isTrue();
        assertThat(FilterStrategy.RETURN_EMPTY_IF_EMPTY.getPredicate().test(null)).isFalse();
    }
}
