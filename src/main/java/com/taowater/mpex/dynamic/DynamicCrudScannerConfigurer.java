package com.taowater.mpex.dynamic;

import lombok.Setter;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

public class DynamicCrudScannerConfigurer extends MapperScannerConfigurer implements InitializingBean {

    @Setter
    private String basePackage;

    /**
     * 后处理 Bean 定义注册表
     *
     * @see MapperScannerConfigurer#postProcessBeanDefinitionRegistry(BeanDefinitionRegistry)
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        // 指定类
        DynamicCrudClassPathScanner scanner = new DynamicCrudClassPathScanner(registry);
        scanner.registerFilters();
        try {
            scanner.scan(StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
 
    @Override
    public void afterPropertiesSet() {
    }

}
