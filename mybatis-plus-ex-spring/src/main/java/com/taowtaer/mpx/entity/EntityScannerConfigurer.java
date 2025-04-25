package com.taowtaer.mpx.entity;

import cn.hutool.core.util.ReflectUtil;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

/**
 * 动态CRUD扫描配置器
 *
 * @author zhu56
 * @date 2025/04/26 00:47
 */
public class EntityScannerConfigurer extends MapperScannerConfigurer implements InitializingBean {

    /**
     * 后处理 Bean 定义注册表
     *
     * @see MapperScannerConfigurer#postProcessBeanDefinitionRegistry(BeanDefinitionRegistry)
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        // 指定类
        EntityClassPathScanner scanner = new EntityClassPathScanner(registry);
        scanner.registerFilters();
        try {
            scanner.scan(StringUtils.tokenizeToStringArray((String) ReflectUtil.getFieldValue(this, "basePackage"), ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterPropertiesSet() {
    }

}
