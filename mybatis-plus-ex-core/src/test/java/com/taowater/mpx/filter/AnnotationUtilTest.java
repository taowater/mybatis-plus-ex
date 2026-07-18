package com.taowater.mpx.filter;

import com.taowater.mpx.filter.op.Eq;
import com.taowater.mpx.filter.op.Filter;
import com.taowater.mpx.filter.op.In;
import com.taowater.mpx.filter.op.Like;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link AnnotationUtil} 组合注解解析：
 * 组合注解（@In/@Eq/@Like）应能推导出其元注解 @Filter 上的 operate，
 * 且 @AliasFor 显式属性（如 field）应覆盖默认值。
 */
class AnnotationUtilTest {

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

        String plain;
    }

    private Filter filterOf(String field) throws NoSuchFieldException {
        Field f = Query.class.getDeclaredField(field);
        return AnnotationUtil.getAnnotation(f, Filter.class);
    }

    @Test
    void in_resolvesOperateFromMetaAnnotation() throws Exception {
        Filter f = filterOf("ids");
        assertThat(f).isNotNull();
        assertThat(f.operate()).isEqualTo(Operator.IN);
        // 未显式指定 field，保持默认空串
        assertThat(f.field()).isEmpty();
    }

    @Test
    void eq_aliasForOverridesField() throws Exception {
        Filter f = filterOf("name");
        assertThat(f.operate()).isEqualTo(Operator.EQ);
        assertThat(f.field()).isEqualTo("userName");
    }

    @Test
    void like_resolvesOperate() throws Exception {
        Filter f = filterOf("keyword");
        assertThat(f.operate()).isEqualTo(Operator.LIKE);
    }

    @Test
    void eq_withoutField_defaultsEmpty() throws Exception {
        Filter f = filterOf("age");
        assertThat(f.operate()).isEqualTo(Operator.EQ);
        assertThat(f.field()).isEmpty();
    }

    @Test
    void plainField_hasNoFilter() throws Exception {
        assertThat(filterOf("plain")).isNull();
    }

    @Test
    void defaultFilterStrategy_isIgnoreEmpty() throws Exception {
        Filter f = filterOf("name");
        assertThat(f.filter()).isEqualTo(FilterStrategy.IGNORE_EMPTY);
    }
}
