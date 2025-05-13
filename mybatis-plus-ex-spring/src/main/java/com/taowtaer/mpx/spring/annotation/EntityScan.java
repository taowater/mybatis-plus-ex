package com.taowtaer.mpx.spring.annotation;

import com.taowtaer.mpx.spring.entity.generate.Generator;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 实体扫描
 *
 * @author zhu56
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({EntityScannerRegistrar.class})
@Repeatable(EntityScans.class)
public @interface EntityScan {

    @AliasFor("basePackages")
    String[] value() default {};

    @AliasFor("value")
    String[] basePackages() default {};

    Class<? extends Generator>[] generators() default {};

    /**
     * 指定本组生成操作基础层对应sqlSessionTemplateRef
     */
    String sqlSessionTemplateRef() default "";

    /**
     * 指定本组生成操作基础层对应sqlSessionFactoryRef
     */
    String sqlSessionFactoryRef() default "";
}
