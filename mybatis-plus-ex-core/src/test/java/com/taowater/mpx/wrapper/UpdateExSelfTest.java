package com.taowater.mpx.wrapper;

import com.taowater.mpx.wrapper.interfaces.UpdateEx;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 自操作（self / setIncrBy / setDecrBy）应使用参数绑定而非字面量拼接，
 * 并且运算符必须经 {@link UpdateEx#safeKeyword} 白名单校验以防注入。
 */
class UpdateExSelfTest {

    @Test
    void setIncrBy_usesParameterBinding_notLiteral() {
        UpdateExWrapper<Object> w = new UpdateExWrapper<>();
        w.setIncrBy("age", 5);

        String set = w.getSqlSet();
        assertThat(set).startsWith("age=age+#{");
        assertThat(set).doesNotContain("age=age+5");
        assertThat(w.getParamNameValuePairs()).containsValue(5);
    }

    @Test
    void setDecrBy_usesParameterBinding() {
        UpdateExWrapper<Object> w = new UpdateExWrapper<>();
        w.setDecrBy("age", 3);

        assertThat(w.getSqlSet()).startsWith("age=age-#{");
        assertThat(w.getParamNameValuePairs()).containsValue(3);
    }

    @Test
    void self_illegalKeyword_throws() {
        UpdateExWrapper<Object> w = new UpdateExWrapper<>();
        assertThatThrownBy(() -> w.self(true, "age", "; DROP TABLE t", 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void safeKeyword_allowsArithmeticOperators() {
        assertThat(UpdateEx.safeKeyword("+")).isEqualTo("+");
        assertThat(UpdateEx.safeKeyword("-")).isEqualTo("-");
        assertThat(UpdateEx.safeKeyword("*")).isEqualTo("*");
        assertThat(UpdateEx.safeKeyword("/")).isEqualTo("/");
        assertThat(UpdateEx.safeKeyword(" + ")).isEqualTo("+");
    }

    @Test
    void safeKeyword_rejectsEverythingElse() {
        assertThatThrownBy(() -> UpdateEx.safeKeyword("%")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> UpdateEx.safeKeyword("++")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> UpdateEx.safeKeyword(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> UpdateEx.safeKeyword("")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void setIncrByX_skipsWhenIncrementEmpty() {
        UpdateExWrapper<Object> w = new UpdateExWrapper<>();
        w.setIncrByX("age", (Integer) null);

        assertThat(w.getSqlSet()).isNull();
    }
}
