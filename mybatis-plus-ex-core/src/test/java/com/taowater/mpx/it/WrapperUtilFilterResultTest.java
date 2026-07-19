package com.taowater.mpx.it;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.taowater.mpx.filter.FilterStrategy;
import com.taowater.mpx.filter.Operator;
import com.taowater.mpx.filter.WrapperUtil;
import com.taowater.mpx.filter.op.Eq;
import com.taowater.mpx.filter.op.Filter;
import com.taowater.mpx.filter.op.Ge;
import com.taowater.mpx.filter.op.Gt;
import com.taowater.mpx.filter.op.In;
import com.taowater.mpx.filter.op.Le;
import com.taowater.mpx.filter.op.Like;
import com.taowater.mpx.filter.op.Lt;
import com.taowater.mpx.filter.op.NotIn;
import com.taowater.mpx.wrapper.QueryExWrapper;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link WrapperUtil} 注解筛选：不仅解析正确，还要在 H2 上验证筛选结果正确。
 */
class WrapperUtilFilterResultTest extends AbstractH2MybatisTest {

    private User alice;
    private User bob;
    private User carol;
    private User dave;

    @BeforeEach
    void seed() {
        alice = newUser("alice", 20);
        bob = newUser("bob", 30);
        carol = newUser("carol", 40);
        dave = newUser("dave", 50);
    }

    private List<User> query(Object param) {
        Wrapper<User> wrapper = WrapperUtil.getWrapper(User.class, param);
        return mapper.selectList(wrapper);
    }

    private List<String> names(Object param) {
        return query(param).stream().map(User::getName).collect(java.util.stream.Collectors.toList());
    }

    // ---------------------------------------------------------------- 各操作符结果

    @Test
    void eq_filtersExactName() {
        EqQuery q = new EqQuery();
        q.setName("bob");
        assertThat(names(q)).containsExactly("bob");
    }

    @Test
    void like_filtersFuzzyName() {
        LikeQuery q = new LikeQuery();
        q.setName("al");
        assertThat(names(q)).containsExactly("alice");
    }

    @Test
    void in_filtersByPrimaryKeys() {
        InQuery q = new InQuery();
        q.setIds(Arrays.asList(alice.getId(), carol.getId()));
        assertThat(names(q)).containsExactlyInAnyOrder("alice", "carol");
    }

    @Test
    void notIn_excludesPrimaryKeys() {
        NotInQuery q = new NotInQuery();
        q.setIds(Collections.singletonList(bob.getId()));
        assertThat(names(q)).containsExactlyInAnyOrder("alice", "carol", "dave");
    }

    @Test
    void gt_filtersAgeGreaterThan() {
        GtQuery q = new GtQuery();
        q.setMinAge(30);
        assertThat(names(q)).containsExactlyInAnyOrder("carol", "dave");
    }

    @Test
    void ge_filtersAgeGreaterOrEqual() {
        GeQuery q = new GeQuery();
        q.setMinAge(30);
        assertThat(names(q)).containsExactlyInAnyOrder("bob", "carol", "dave");
    }

    @Test
    void lt_filtersAgeLessThan() {
        LtQuery q = new LtQuery();
        q.setMaxAge(30);
        assertThat(names(q)).containsExactly("alice");
    }

    @Test
    void le_filtersAgeLessOrEqual() {
        LeQuery q = new LeQuery();
        q.setMaxAge(30);
        assertThat(names(q)).containsExactlyInAnyOrder("alice", "bob");
    }

    @Test
    void ne_viaDirectFilter_excludesName() {
        NeQuery q = new NeQuery();
        q.setName("bob");
        assertThat(names(q)).containsExactlyInAnyOrder("alice", "carol", "dave");
    }

    @Test
    void fieldRemap_eqOnAliasProperty_hitsNameColumn() {
        RemapQuery q = new RemapQuery();
        q.setKeyword("carol");
        assertThat(names(q)).containsExactly("carol");
    }

    @Test
    void combined_likeAndGtAndIn_intersects() {
        UserQuery q = new UserQuery();
        q.setName("a"); // alice, carol 不含 "a"? alice 含 a, carol 含 a? "carol" contains a, "alice" contains a, "dave" contains a, "bob" no
        // LIKE %a% -> alice, carol, dave
        q.setMinAge(25); // age > 25 -> bob, carol, dave; intersect -> carol, dave
        q.setIds(Arrays.asList(alice.getId(), carol.getId(), dave.getId())); // still carol, dave
        assertThat(names(q)).containsExactlyInAnyOrder("carol", "dave");
    }

    // ---------------------------------------------------------------- 过滤策略对结果的影响

    @Test
    void ignoreEmpty_nullValue_doesNotFilter_returnsAll() {
        LikeQuery q = new LikeQuery();
        q.setName(null);
        assertThat(names(q)).containsExactlyInAnyOrder("alice", "bob", "carol", "dave");
    }

    @Test
    void ignoreEmpty_blankLike_doesNotFilter_returnsAll() {
        LikeQuery q = new LikeQuery();
        q.setName("");
        assertThat(names(q)).containsExactlyInAnyOrder("alice", "bob", "carol", "dave");
    }

    @Test
    void returnEmptyIfEmpty_nullValue_marksWrapperNotExecutable() {
        ShortCircuitQuery q = new ShortCircuitQuery();
        q.setName(null);

        QueryExWrapper<User> wrapper = (QueryExWrapper<User>) WrapperUtil.getWrapper(User.class, q);
        assertThat(wrapper.needExecute()).isFalse();
    }

    @Test
    void ignore_strategy_skipsAnnotatedField_evenWhenValuePresent() {
        IgnoreQuery q = new IgnoreQuery();
        q.setName("bob"); // 有值也不应参与条件
        assertThat(names(q)).containsExactlyInAnyOrder("alice", "bob", "carol", "dave");
    }

    @Test
    void always_nullEq_stillAppliesCondition_returnsEmpty() {
        // ALWAYS + eq null：条件仍组装，库中无 name IS NULL 行
        AlwaysEqQuery q = new AlwaysEqQuery();
        q.setName(null);
        assertThat(names(q)).isEmpty();
    }

    @Test
    void plainFieldsWithoutAnnotation_areIgnored() {
        MixedQuery q = new MixedQuery();
        q.setName("bob");
        q.setIgnoredNote("should-not-affect");
        assertThat(names(q)).containsExactly("bob");
    }

    // ---------------------------------------------------------------- 查询 DTO

    @Data
    static class EqQuery {
        @Eq
        private String name;
    }

    @Data
    static class LikeQuery {
        @Like
        private String name;
    }

    @Data
    static class InQuery {
        @In(field = "id")
        private List<Long> ids;
    }

    @Data
    static class NotInQuery {
        @NotIn(field = "id")
        private List<Long> ids;
    }

    @Data
    static class GtQuery {
        @Gt(field = "age")
        private Integer minAge;
    }

    @Data
    static class GeQuery {
        @Ge(field = "age")
        private Integer minAge;
    }

    @Data
    static class LtQuery {
        @Lt(field = "age")
        private Integer maxAge;
    }

    @Data
    static class LeQuery {
        @Le(field = "age")
        private Integer maxAge;
    }

    @Data
    static class NeQuery {
        @Filter(operate = Operator.NE)
        private String name;
    }

    @Data
    static class RemapQuery {
        @Eq(field = "name")
        private String keyword;
    }

    @Data
    static class ShortCircuitQuery {
        @Eq(filter = FilterStrategy.RETURN_EMPTY_IF_EMPTY)
        private String name;
    }

    @Data
    static class IgnoreQuery {
        @Eq(filter = FilterStrategy.IGNORE)
        private String name;
    }

    @Data
    static class AlwaysEqQuery {
        @Eq(filter = FilterStrategy.ALWAYS)
        private String name;
    }

    @Data
    static class MixedQuery {
        @Eq
        private String name;
        /** 无注解，不应参与筛选 */
        private String ignoredNote;
    }
}
