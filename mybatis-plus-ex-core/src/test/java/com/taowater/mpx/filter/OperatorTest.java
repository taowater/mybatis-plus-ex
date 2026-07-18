package com.taowater.mpx.filter;

import com.taowater.mpx.wrapper.QueryExWrapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link Operator} IN / NOT_IN 对集合、数组、单值的分流行为。
 * <p>
 * 使用字符串列名的 {@link QueryExWrapper}，无需初始化 TableInfo，纯内存断言生成的 SQL 片段与绑定参数。
 * 注意：MyBatis-Plus 的参数值是在 {@code getSqlSegment()} 时惰性写入 paramNameValuePairs 的，
 * 因此需先触发一次 SQL 片段生成，再统计绑定参数。
 */
class OperatorTest {

    /**
     * 统计 SQL 片段中的占位符 {@code #{} } 数量。
     */
    private int placeholderCount(String segment) {
        int count = 0;
        int idx = 0;
        while ((idx = segment.indexOf("#{", idx)) >= 0) {
            count++;
            idx += 2;
        }
        return count;
    }

    @Test
    void in_collection_expandsEachElementAsParam() {
        QueryExWrapper<Object> w = new QueryExWrapper<>();
        Operator.IN.getFun().apply(w, true, "id", Arrays.asList(1, 2, 3));

        String segment = w.getSqlSegment();
        assertThat(segment).containsIgnoringCase("IN");
        assertThat(placeholderCount(segment)).isEqualTo(3);
        assertThat(w.getParamNameValuePairs()).hasSize(3);
    }

    @Test
    void in_array_expandsEachElementAsParam() {
        QueryExWrapper<Object> w = new QueryExWrapper<>();
        Operator.IN.getFun().apply(w, true, "id", new Integer[]{1, 2});

        assertThat(placeholderCount(w.getSqlSegment())).isEqualTo(2);
        assertThat(w.getParamNameValuePairs()).hasSize(2);
    }

    @Test
    void in_primitiveArray_expandsEachElementAsParam() {
        QueryExWrapper<Object> w = new QueryExWrapper<>();
        Operator.IN.getFun().apply(w, true, "id", new int[]{7, 8, 9});

        assertThat(placeholderCount(w.getSqlSegment())).isEqualTo(3);
        assertThat(w.getParamNameValuePairs()).hasSize(3);
    }

    @Test
    void in_singleValue_treatedAsOneElement() {
        QueryExWrapper<Object> w = new QueryExWrapper<>();
        Operator.IN.getFun().apply(w, true, "id", 5);

        assertThat(placeholderCount(w.getSqlSegment())).isEqualTo(1);
        assertThat(w.getParamNameValuePairs()).hasSize(1);
    }

    @Test
    void notIn_collection_expandsEachElementAsParam() {
        QueryExWrapper<Object> w = new QueryExWrapper<>();
        Operator.NOT_IN.getFun().apply(w, true, "id", Arrays.asList(1, 2, 3, 4));

        String segment = w.getSqlSegment();
        assertThat(segment).containsIgnoringCase("NOT IN");
        assertThat(placeholderCount(segment)).isEqualTo(4);
        assertThat(w.getParamNameValuePairs()).hasSize(4);
    }

    @Test
    void eq_singleValue_bindsOneParam() {
        QueryExWrapper<Object> w = new QueryExWrapper<>();
        Operator.EQ.getFun().apply(w, true, "name", "tao");

        assertThat(w.getSqlSegment()).contains("=");
        assertThat(w.getParamNameValuePairs()).containsValue("tao");
    }
}
