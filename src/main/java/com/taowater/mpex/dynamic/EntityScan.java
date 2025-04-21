/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.taowater.mpex.dynamic;

import org.mybatis.spring.annotation.MapperScan;
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
@Import({DynamicCrudScannerRegistrar.class})
@Repeatable(EntityScans.class)
public @interface EntityScan {

    @AliasFor("basePackages")
    String[] value() default {};

    @AliasFor("value")
    String[] basePackages() default {};

    /**
     * 指定本组生成操作基础层对应sqlSessionTemplateRef
     *
     * @see MapperScan#sqlSessionTemplateRef()
     */
    String sqlSessionTemplateRef() default "";

    /**
     * 指定本组生成操作基础层对应sqlSessionFactoryRef
     *
     * @see MapperScan#sqlSessionFactoryRef()
     */
    String sqlSessionFactoryRef() default "";
}
