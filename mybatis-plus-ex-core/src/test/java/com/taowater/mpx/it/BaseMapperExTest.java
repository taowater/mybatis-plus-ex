package com.taowater.mpx.it;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taowater.mpx.wrapper.LambdaUpdateExWrapper;
import com.taowater.ztream.Any;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 只覆盖本库 {@code BaseMapper} 相对 MyBatis-Plus 原版<strong>新增或改动</strong>的方法，
 * MP 原有方法（insert/selectById/updateById 等）不在此测试范围内。
 */
class BaseMapperExTest extends AbstractH2MybatisTest {

    // ---------------------------------------------------------------- selectCount

    @Test
    void selectCount_noArg_countsAll() {
        newUser("a", 1);
        newUser("b", 2);
        assertThat(mapper.selectCount()).isEqualTo(2L);
    }

    @Test
    void selectCount_consumer_countsMatching() {
        newUser("a", 10);
        newUser("b", 20);
        assertThat(mapper.selectCount(w -> w.gt(User::getAge, 15))).isEqualTo(1L);
    }

    @Test
    void selectCount_consumer_shortCircuitOnEmptyRequired() {
        newUser("a", 10);
        // eqR 必需参数为空 -> 不查库直接返回 0
        assertThat(mapper.selectCount(w -> w.eqR(User::getName, null))).isEqualTo(0L);
    }

    // ---------------------------------------------------------------- selectList

    @Test
    void selectList_noArg_returnsAll() {
        newUser("a", 1);
        newUser("b", 2);
        assertThat(mapper.selectList()).hasSize(2);
    }

    @Test
    void selectList_consumer_filters() {
        newUser("a", 10);
        newUser("b", 20);
        assertThat(mapper.selectList(w -> w.gt(User::getAge, 15))).hasSize(1);
    }

    @Test
    void selectList_withLimit_appliesDialectLimit() {
        newUser("a", 1);
        newUser("b", 2);
        newUser("c", 3);

        assertThat(mapper.selectList(w -> w.limit(2))).hasSize(2);
        assertThat(mapper.selectList()).hasSize(3);
    }

    @Test
    void selectList_byAnnotatedParam() {
        newUser("alpha", 30);
        newUser("alphabet", 40);
        newUser("gamma", 50);

        UserQuery query = new UserQuery();
        query.setName("alph");
        query.setMinAge(20);

        assertThat(mapper.selectList(query)).extracting(User::getName)
                .containsExactlyInAnyOrder("alpha", "alphabet");
    }

    @Test
    void selectList_returnTypeProjection() {
        newUser("proj", 1);
        List<NameOnly> list = mapper.selectList(NameOnly.class, w -> w.eq(User::getName, "proj"));
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getName()).isEqualTo("proj");
    }

    // ---------------------------------------------------------------- selectPage（改动：分页前清除 limit）

    /**
     * 该重载的独有逻辑是：分页前会清空 wrapper 上可能残留的 {@code limit}，避免与分页冲突。
     * <p>
     * 说明：MP 3.5.17 的物理分页需要 jsqlparser 模块（仅提供 Java 11 字节码），与本库 Java 8 基线不兼容，
     * 故此处不加载分页内部拦截器，仅验证该重载能清除 limit 并正常返回匹配数据（不被 limit 截断）。
     */
    @Test
    void selectPage_consumer_clearsLimitAndReturnsData() {
        newUser("a", 1);
        newUser("b", 2);
        newUser("c", 3);

        Page<User> page = new Page<>(1, 2);
        // consumer 中即便设置了 limit(1)，也应被清除，不影响结果集
        Page<User> result = mapper.selectPage(page, w -> w.gt(User::getAge, 0).limit(1));

        assertThat(result).isSameAs(page);
        assertThat(result.getRecords()).extracting(User::getName)
                .containsExactlyInAnyOrder("a", "b", "c");
    }

    // ---------------------------------------------------------------- selectZtream

    @Test
    void selectZtream_consumer_returnsStream() {
        newUser("a", 10);
        newUser("b", 20);

        List<String> names = mapper.selectZtream(w -> w.gt(User::getAge, 5))
                .map(User::getName)
                .toList();
        assertThat(names).containsExactlyInAnyOrder("a", "b");
    }

    @Test
    void selectZtream_noArg_returnsAll() {
        newUser("a", 1);
        assertThat(mapper.selectZtream().toList()).hasSize(1);
    }

    // ---------------------------------------------------------------- selectOne / selectOneX

    @Test
    void selectOne_consumer_defaultNoThrow() {
        newUser("only", 1);
        User one = mapper.selectOne(w -> w.eq(User::getName, "only"));
        assertThat(one).isNotNull();
        assertThat(one.getName()).isEqualTo("only");
    }

    @Test
    void selectOne_consumer_throwOnMultiple() {
        newUser("dup", 1);
        newUser("dup", 2);
        assertThatThrownBy(() -> mapper.selectOne(w -> w.eq(User::getName, "dup"), true))
                .isInstanceOf(TooManyResultsException.class);
    }

    @Test
    void selectOne_consumer_noThrowReturnsFirst() {
        newUser("dup", 1);
        newUser("dup", 2);
        assertThat(mapper.selectOne(w -> w.eq(User::getName, "dup"), false)).isNotNull();
    }

    @Test
    void selectOneX_present_and_absent() {
        newUser("x", 1);

        Any<User> present = mapper.selectOneX(w -> w.eq(User::getName, "x"));
        assertThat(present.orElse(null)).isNotNull();

        Any<User> absent = mapper.selectOneX(w -> w.eq(User::getName, "none"));
        assertThat(absent.orElse(null)).isNull();
    }

    @Test
    void selectOneX_returnTypeProjection() {
        newUser("proj", 1);
        Any<NameOnly> any = mapper.selectOneX(NameOnly.class, w -> w.eq(User::getName, "proj"));
        assertThat(any.orElse(null)).isNotNull();
        assertThat(any.get().getName()).isEqualTo("proj");
    }

    @Test
    void selectOne_returnTypeProjection() {
        newUser("proj", 1);
        NameOnly one = mapper.selectOne(NameOnly.class, w -> w.eq(User::getName, "proj"));
        assertThat(one).isNotNull();
        assertThat(one.getName()).isEqualTo("proj");
    }

    /**
     * 非 QueryEx 的普通 wrapper 走 {@code applySelectOneLimit} 的 {@code LIMIT n} 分支。
     */
    @Test
    void selectOne_plainWrapper_appliesLastLimit() {
        newUser("dup", 1);
        newUser("dup", 2);

        // throwEx=false -> LIMIT 1 -> 仅取一条，不抛异常
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("name", "dup");
        assertThat(mapper.selectOne(qw, false)).isNotNull();

        // throwEx=true -> LIMIT 2 -> 命中两条 -> 抛异常
        QueryWrapper<User> qw2 = new QueryWrapper<>();
        qw2.eq("name", "dup");
        assertThatThrownBy(() -> mapper.selectOne(qw2, true))
                .isInstanceOf(TooManyResultsException.class);
    }

    // ---------------------------------------------------------------- selectExists

    @Test
    void selectExists_consumer_reflectsData() {
        newUser("exist", 1);
        assertThat(mapper.selectExists(w -> w.eq(User::getName, "exist"))).isTrue();
        assertThat(mapper.selectExists(w -> w.eq(User::getName, "missing"))).isFalse();
    }

    // ---------------------------------------------------------------- update / delete

    @Test
    void update_entityConsumer_appliesSet() {
        User u = newUser("old", 1);
        int rows = mapper.update(new User(), w -> w.eq(User::getId, u.getId()).set(User::getName, "new"));
        assertThat(rows).isEqualTo(1);
        assertThat(mapper.selectById(u.getId()).getName()).isEqualTo("new");
    }

    @Test
    void update_consumer_selfIncrement() {
        User u = newUser("inc", 10);
        int rows = mapper.update(w -> w.eq(User::getId, u.getId()).setIncrBy(User::getAge, 5));
        assertThat(rows).isEqualTo(1);
        assertThat(mapper.selectById(u.getId()).getAge()).isEqualTo(15);
    }

    @Test
    void update_wrapperOverride_appliesSet() {
        User u = newUser("old", 1);

        LambdaUpdateExWrapper<User> uw = new LambdaUpdateExWrapper<>();
        uw.eq(User::getId, u.getId()).set(User::getName, "override");
        int rows = mapper.update(uw);

        assertThat(rows).isEqualTo(1);
        assertThat(mapper.selectById(u.getId()).getName()).isEqualTo("override");
    }

    @Test
    void delete_consumer_removesMatching() {
        newUser("keep", 1);
        User del = newUser("del", 2);

        int rows = mapper.delete(w -> w.eq(User::getId, del.getId()));
        assertThat(rows).isEqualTo(1);
        assertThat(mapper.selectList()).extracting(User::getName).containsExactly("keep");
    }

    // ---------------------------------------------------------------- groupBy / mapBy

    @Test
    void groupBy_field_values() {
        newUser("a", 1);
        newUser("a", 2);
        newUser("b", 3);

        Map<String, List<User>> grouped = mapper.groupBy(User::getName, Arrays.asList("a", "b"));
        assertThat(grouped.get("a")).hasSize(2);
        assertThat(grouped.get("b")).hasSize(1);
    }

    @Test
    void groupBy_withValueMapper() {
        newUser("a", 1);
        newUser("a", 2);

        Map<String, List<Integer>> grouped = mapper.groupBy(User::getName, java.util.Collections.singletonList("a"), User::getAge);
        assertThat(grouped.get("a")).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void groupBy_emptyValues_returnsEmpty() {
        newUser("a", 1);
        assertThat(mapper.groupBy(User::getName, java.util.Collections.emptyList())).isEmpty();
    }

    @Test
    void mapBy_field_values() {
        User a = newUser("a", 1);
        User b = newUser("b", 2);

        Map<Long, User> map = mapper.mapBy(User::getId, Arrays.asList(a.getId(), b.getId()));
        assertThat(map).hasSize(2);
        assertThat(map.get(a.getId()).getName()).isEqualTo("a");
    }

    @Test
    void mapBy_withValueMapper() {
        User a = newUser("a", 1);
        Map<Long, String> map = mapper.mapBy(User::getId, java.util.Collections.singletonList(a.getId()), User::getName);
        assertThat(map.get(a.getId())).isEqualTo("a");
    }

    @Test
    void mapBy_emptyValues_returnsEmpty() {
        newUser("a", 1);
        assertThat(mapper.mapBy(User::getId, java.util.Collections.emptyList())).isEmpty();
    }
}
