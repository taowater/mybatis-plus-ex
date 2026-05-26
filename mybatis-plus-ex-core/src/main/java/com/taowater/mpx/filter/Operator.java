package com.taowater.mpx.filter;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import io.vavr.Function4;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作类型
 */
@Getter
@AllArgsConstructor
public enum Operator {

    EQ(AbstractWrapper::eq),
    NE(AbstractWrapper::ne),
    IN(AbstractWrapper::in),
    NOT_IN(AbstractWrapper::notIn),
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
}
