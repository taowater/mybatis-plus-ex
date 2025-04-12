package com.taowater.mpex.method;

import org.springframework.context.annotation.Bean;

public class MybatisPlusExConfig {

    @Bean
    public CustomSqlInjector customSqlInjector() {
        return new CustomSqlInjector();
    }
}