package com.taowater.mpx.filter;

import com.taowater.mpx.filter.op.Eq;
import com.taowater.mpx.filter.op.Filter;
import com.taowater.mpx.filter.op.In;
import com.taowater.mpx.filter.op.Like;
import com.taowater.mpx.filter.op.NotIn;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 拆开读：operate 取自注解类型上的元注解 {@link Filter}，field/filter 取自外层注解实例。
 */
class FilterAnnTest {

    @SuppressWarnings("unused")
    static class Query {
        @In
        java.util.List<Long> ids;

        @Eq(field = "userName")
        String name;

        @Like
        String keyword;

        @Eq
        Integer age;

        @Filter(operate = Operator.NE, field = "status", filter = FilterStrategy.ALLWAYS)
        Integer status;

        @NotIn(field = "id", filter = FilterStrategy.IGNORE)
        java.util.List<Long> excludeIds;

        String plain;
    }

    private FilterAnn of(String field) throws NoSuchFieldException {
        Field f = Query.class.getDeclaredField(field);
        return FilterAnn.resolve(f);
    }

    @Test
    void in_operateFromMeta_defaultsEmptyField() throws Exception {
        FilterAnn a = of("ids");
        assertThat(a).isNotNull();
        assertThat(a.getOperate()).isEqualTo(Operator.IN);
        assertThat(a.getField()).isEmpty();
        assertThat(a.getStrategy()).isEqualTo(FilterStrategy.IGNORE_EMPTY);
    }

    @Test
    void eq_fieldFromOuterInstance() throws Exception {
        FilterAnn a = of("name");
        assertThat(a.getOperate()).isEqualTo(Operator.EQ);
        assertThat(a.getField()).isEqualTo("userName");
    }

    @Test
    void like_operateFromMeta() throws Exception {
        assertThat(of("keyword").getOperate()).isEqualTo(Operator.LIKE);
    }

    @Test
    void eq_withoutField_defaultsEmpty() throws Exception {
        FilterAnn a = of("age");
        assertThat(a.getOperate()).isEqualTo(Operator.EQ);
        assertThat(a.getField()).isEmpty();
    }

    @Test
    void directFilter_readsOperateAndAttrsFromInstance() throws Exception {
        FilterAnn a = of("status");
        assertThat(a.getOperate()).isEqualTo(Operator.NE);
        assertThat(a.getField()).isEqualTo("status");
        assertThat(a.getStrategy()).isEqualTo(FilterStrategy.ALLWAYS);
    }

    @Test
    void notIn_strategyFromOuterInstance() throws Exception {
        FilterAnn a = of("excludeIds");
        assertThat(a.getOperate()).isEqualTo(Operator.NOT_IN);
        assertThat(a.getField()).isEqualTo("id");
        assertThat(a.getStrategy()).isEqualTo(FilterStrategy.IGNORE);
    }

    @Test
    void plainField_resolvesNull() throws Exception {
        assertThat(of("plain")).isNull();
    }

    @Test
    void typeMetaIsCached_sameResultOnRepeatedResolve() throws Exception {
        FilterAnn a1 = of("ids");
        FilterAnn a2 = of("ids");
        assertThat(a1.getOperate()).isEqualTo(a2.getOperate());
        assertThat(a1.getStrategy()).isEqualTo(a2.getStrategy());
    }
}
