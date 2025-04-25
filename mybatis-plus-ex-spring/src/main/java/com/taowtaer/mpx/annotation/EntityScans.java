package com.taowtaer.mpx.annotation;

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
@Import(EntityScannerRegistrar.RepeatingRegistrar.class)
public @interface EntityScans {
    EntityScan[] value();
}
