package com.taowtaer.mpx.spring.entity;

import cn.hutool.core.util.ReflectUtil;
import com.taowtaer.mpx.spring.entity.generate.Generator;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

/**
 * 动态CRUD扫描配置器
 *
 * @author zhu56
 * @date 2025/04/26 00:47
 */
public class EntityScannerConfigurer implements BeanDefinitionRegistryPostProcessor, InitializingBean {
    @Setter
    private Class<? extends Generator<?>>[] generators;
    @Getter
    @Setter
    private String basePackage;
    @Getter
    @Setter
    private boolean processPropertyPlaceHolders;
    @Getter
    @Setter
    private SqlSessionTemplate sqlSessionTemplate;
    @Getter
    @Setter
    private String sqlSessionFactoryBeanName;
    @Getter
    @Setter
    private String sqlSessionTemplateBeanName;

    /**
     * 后处理 Bean 定义注册表
     *
     * @see MapperScannerConfigurer#postProcessBeanDefinitionRegistry(BeanDefinitionRegistry)
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        // 指定类
        EntityClassPathScanner scanner = new EntityClassPathScanner(registry, generators);
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

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }
}
