package com.taowater.mpex.dynamic;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 实体扫描
 *
 * @author zhu56
 * @see EntityScan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(DynamicCrudScannerRegistrar.RepeatingRegistrar.class)
public @interface EntityScans {
    EntityScan[] value();
}
