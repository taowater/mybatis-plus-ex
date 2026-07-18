package com.taowater.mpx.filter;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import io.vavr.Function4;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Array;
import java.util.Collection;

/**
 * 操作类型
 */
@Getter
@AllArgsConstructor
public enum Operator {

    EQ(AbstractWrapper::eq),
    NE(AbstractWrapper::ne),
    IN(Operator::in),
    NOT_IN(Operator::notIn),
    LIKE(AbstractWrapper::like),
    LT(AbstractWrapper::lt),
    LE(AbstractWrapper::le),
    GT(AbstractWrapper::gt),
    GE(AbstractWrapper::ge),
    ;

    /**
     * 操作方法
     */
    private final Function4<AbstractWrapper<?, String, ?>, Boolean, String, Object, ?> fun;

    /**
     * Collection / 数组走对应重载，避免被当成单元素 Object...。
     */
    private static Object in(AbstractWrapper<?, String, ?> w, Boolean condition, String column, Object val) {
        if (val instanceof Collection) {
            return w.in(condition, column, (Collection<?>) val);
        }
        if (val instanceof Object[]) {
            return w.in(condition, column, (Object[]) val);
        }
        if (val != null && val.getClass().isArray()) {
            return w.in(condition, column, toObjectArray(val));
        }
        return w.in(condition, column, val);
    }

    private static Object notIn(AbstractWrapper<?, String, ?> w, Boolean condition, String column, Object val) {
        if (val instanceof Collection) {
            return w.notIn(condition, column, (Collection<?>) val);
        }
        if (val instanceof Object[]) {
            return w.notIn(condition, column, (Object[]) val);
        }
        if (val != null && val.getClass().isArray()) {
            return w.notIn(condition, column, toObjectArray(val));
        }
        return w.notIn(condition, column, val);
    }

    private static Object[] toObjectArray(Object array) {
        int len = Array.getLength(array);
        Object[] result = new Object[len];
        for (int i = 0; i < len; i++) {
            result[i] = Array.get(array, i);
        }
        return result;
    }
}
