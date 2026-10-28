package com.taowater.mpx.it;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.taowater.mpx.wrapper.LambdaQueryExWrapper;
import com.taowater.mpx.wrapper.QueryExWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * H2 执行默认 LIMIT 分支，验证条数限制及普通 wrapper 的兼容性。
 */
class SelectListLimitTest extends AbstractH2MybatisTest {

    @BeforeEach
    void seed() {
        newUser("first", 20);
        newUser("second", 20);
        newUser("other", 10);
    }

    @ParameterizedTest
    @CsvSource({"false, 0", "false, 1", "false, 2", "true, 0", "true, 1", "true, 2"})
    void limit_shouldRestrictRowsForBothWrapperTypes(boolean lambda, int limit) {
        Wrapper<User> wrapper = lambda
                ? new LambdaQueryExWrapper<>(User.class).eq(User::getAge, 20).orderByAsc(User::getId).limit(limit)
                : new QueryExWrapper<>(User.class).eq("age", 20).orderByAsc("id").limit(limit);

        assertThat(mapper.selectList(wrapper)).extracting(User::getName)
                .containsExactly(java.util.Arrays.copyOf(new String[]{"first", "second"}, limit));
    }

    @Test
    void noLimit_shouldKeepAllMatchingRows() {
        assertThat(mapper.selectList(new QueryExWrapper<User>().eq("age", 20))).hasSize(2);
        assertThat(mapper.selectList(new LambdaQueryExWrapper<>(User.class).eq(User::getAge, 20))).hasSize(2);
        assertThat(mapper.selectList((Wrapper<User>) null)).hasSize(3);
        assertThat(mapper.selectList(new QueryWrapper<User>().eq("age", 20))).hasSize(2);
    }

    @Test
    void nullLimit_shouldRemoveRestriction() {
        QueryExWrapper<User> wrapper = new QueryExWrapper<User>().eq("age", 20).limit(1);
        wrapper.setLimit(null);

        assertThat(mapper.selectList(wrapper)).hasSize(2);
    }

    @Test
    void returnTypeQuery_shouldAlsoRespectLimit() {
        assertThat(mapper.selectList(String.class, new LambdaQueryExWrapper<>(User.class)
                .select(User::getName).eq(User::getAge, 20).orderByAsc(User::getId).limit(1)))
                .containsExactly("first");
    }
}
