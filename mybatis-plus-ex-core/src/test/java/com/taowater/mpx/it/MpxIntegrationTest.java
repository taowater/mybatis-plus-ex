package com.taowater.mpx.it;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.taowater.mpx.filter.WrapperUtil;
import com.taowater.mpx.interceptor.ReturnTypeInterceptor;
import com.taowater.mpx.method.ExMethodSqlInjector;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 基于 H2 内存库的端到端集成测试，验证自定义 SQL 注入器（{@link ExMethodSqlInjector}）、
 * 拦截器（{@link ReturnTypeInterceptor}）与 {@link WrapperUtil} 在真实执行下的行为。
 * <p>
 * 通过给注入器显式传入 {@link DbType#H2}，无需依赖运行期数据库探测，即可稳定测试方言相关逻辑。
 */
class MpxIntegrationTest extends AbstractH2MybatisTest {

    @Test
    void projection_returnTypeInterceptor_mapsToDto() {
        newUser("proj", 42);

        List<NameOnly> names = mapper.selectList(NameOnly.class, w -> w.eq(User::getName, "proj"));
        assertThat(names).hasSize(1);
        assertThat(names.get(0).getName()).isEqualTo("proj");
    }

    @Test
    void wrapperUtil_annotationDrivenQuery_executes() {
        User a = newUser("alpha", 30);
        User b = newUser("alphabet", 40);
        newUser("gamma", 50);

        UserQuery query = new UserQuery();
        query.setName("alph");
        query.setMinAge(20);
        query.setIds(Arrays.asList(a.getId(), b.getId()));

        Wrapper<User> wrapper = WrapperUtil.getWrapper(User.class, query);
        List<User> result = mapper.selectList(wrapper);

        assertThat(result).extracting(User::getName)
                .containsExactlyInAnyOrder("alpha", "alphabet");
    }

    @Test
    void wrapperUtil_includesPrimaryKeyMapping() {
        // @In(field = "id") 依赖主键被纳入 property->column 映射
        User a = newUser("pk", 1);

        UserQuery query = new UserQuery();
        query.setIds(java.util.Collections.singletonList(a.getId()));

        Wrapper<User> wrapper = WrapperUtil.getWrapper(User.class, query);
        assertThat(wrapper.getSqlSegment()).containsIgnoringCase("id");

        List<User> result = mapper.selectList(wrapper);
        assertThat(result).hasSize(1);
    }
}
