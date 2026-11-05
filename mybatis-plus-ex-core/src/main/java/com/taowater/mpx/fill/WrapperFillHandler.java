package com.taowater.mpx.fill;

/**
 * Wrapper 更新的默认赋值。仅填充当前实体实际映射、且尚未参与 SET 的属性。
 * 实现应无状态；Spring Boot 下声明为 Bean 即可全局生效。
 */
@FunctionalInterface
public interface WrapperFillHandler {

    void updateFill(WrapperFillContext context);
}
