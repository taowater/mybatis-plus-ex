package com.taowater.mpx.spring.entity;

import com.taowater.mpx.spring.entity.generate.Generator;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * 动态CRUD扫描配置器
 *
 * @author zhu56
 * @date 2025/04/26 00:47
 */
public class EntityScannerConfigurer implements BeanDefinitionRegistryPostProcessor, InitializingBean, EnvironmentAware {

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
    private SqlSessionFactory sqlSessionFactory;
    @Getter
    @Setter
    private SqlSessionTemplate sqlSessionTemplate;
    @Getter
    @Setter
    private String sqlSessionFactoryBeanName;
    @Getter
    @Setter
    private String sqlSessionTemplateBeanName;
    @Getter
    @Setter
    private String defaultScope;

    private Environment environment;

    /**
     * 后处理 Bean 定义注册表
     *
     * @see MapperScannerConfigurer#postProcessBeanDefinitionRegistry(BeanDefinitionRegistry)
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if (this.processPropertyPlaceHolders) {
            processPropertyPlaceHolders();
        }
        EntityClassPathScanner scanner = new EntityClassPathScanner(registry, generators);
        scanner.setSqlSessionFactory(sqlSessionFactory);
        scanner.setSqlSessionTemplate(sqlSessionTemplate);
        scanner.setSqlSessionFactoryBeanName(sqlSessionFactoryBeanName);
        scanner.setSqlSessionTemplateBeanName(sqlSessionTemplateBeanName);
        scanner.setDefaultScope(defaultScope);
        scanner.registerFilters();
        try {
            scanner.scan(StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析 {@code basePackage} / SqlSession bean 名 / defaultScope 中的 {@code ${...}} 占位符。
     */
    private void processPropertyPlaceHolders() {
        if (this.environment == null) {
            return;
        }
        if (StringUtils.hasText(this.basePackage)) {
            this.basePackage = this.environment.resolvePlaceholders(this.basePackage);
        }
        if (StringUtils.hasText(this.sqlSessionFactoryBeanName)) {
            this.sqlSessionFactoryBeanName = this.environment.resolvePlaceholders(this.sqlSessionFactoryBeanName);
        }
        if (StringUtils.hasText(this.sqlSessionTemplateBeanName)) {
            this.sqlSessionTemplateBeanName = this.environment.resolvePlaceholders(this.sqlSessionTemplateBeanName);
        }
        if (StringUtils.hasText(this.defaultScope)) {
            this.defaultScope = this.environment.resolvePlaceholders(this.defaultScope);
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void afterPropertiesSet() {
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }
}
