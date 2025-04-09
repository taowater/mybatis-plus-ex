package com.taowater.mpex;

import org.springframework.context.annotation.Bean;

public class MybatisPlusExConfig {

    @Bean
    public CustomSqlInjector customSqlInjector() {
        return new CustomSqlInjector();
    }
}