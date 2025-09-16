package com.taowater.mpx.autoconfigure;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.taowater.mpx.interceptor.ReturnTypeInterceptor;
import com.taowater.mpx.method.ExMethodSqlInjector;
import com.taowtaer.mpx.spring.entity.EntityScannerConfigurer;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * mpx自动装配
 *
 * @author zhu56
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class})
@EnableConfigurationProperties(MybatisPlusProperties.class)
public class MyBatisPlusExConfig {


    @Bean
    @ConditionalOnMissingBean(ExMethodSqlInjector.class)
    public ExMethodSqlInjector exMethodSqlInjector() {
        return new ExMethodSqlInjector();
    }

    @Bean
    @ConditionalOnMissingBean(ReturnTypeInterceptor.class)
    public ReturnTypeInterceptor exInterceptor() {
        return new ReturnTypeInterceptor();
    }

    public static class AutoConfiguredEntityScannerRegistrar
            implements BeanFactoryAware, EnvironmentAware, ImportBeanDefinitionRegistrar {


        private BeanFactory beanFactory;
        private Environment environment;

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

            if (!AutoConfigurationPackages.has(this.beanFactory)) {
                log.debug("Could not determine auto-configuration package, automatic mapper scanning disabled.");
                return;
            }

            log.debug("Searching for mappers annotated with @Mapper");

            List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
            if (log.isDebugEnabled()) {
                packages.forEach(pkg -> log.debug("Using auto-configuration base package '{}'", pkg));
            }
            Class<?> configurerClass = EntityScannerConfigurer.class;
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(configurerClass);
            builder.addPropertyValue("processPropertyPlaceHolders", true);
            builder.addPropertyValue("basePackage", StringUtils.collectionToCommaDelimitedString(packages));
            BeanWrapper beanWrapper = new BeanWrapperImpl(configurerClass);
            Set<String> propertyNames = Stream.of(beanWrapper.getPropertyDescriptors()).map(PropertyDescriptor::getName)
                    .collect(Collectors.toSet());
            if (propertyNames.contains("lazyInitialization")) {
                // Need to mybatis-spring 2.0.2+
                builder.addPropertyValue("lazyInitialization", "${mybatis-plus.lazy-initialization:${mybatis.lazy-initialization:false}}");
            }
            if (propertyNames.contains("defaultScope")) {
                // Need to mybatis-spring 2.0.6+
                builder.addPropertyValue("defaultScope", "${mybatis-plus.mapper-default-scope:}");
            }

            // for spring-native
            Boolean injectSqlSession = environment.getProperty("mybatis-plus.inject-sql-session-on-mapper-scan", Boolean.class);
            if (injectSqlSession == null) {
                injectSqlSession = environment.getProperty("mybatis.inject-sql-session-on-mapper-scan", Boolean.class, Boolean.TRUE);
            }
            if (injectSqlSession && this.beanFactory instanceof ListableBeanFactory) {
                ListableBeanFactory listableBeanFactory = (ListableBeanFactory) this.beanFactory;
                Optional<String> sqlSessionTemplateBeanName = Optional
                        .ofNullable(getBeanNameForType(SqlSessionTemplate.class, listableBeanFactory));
                Optional<String> sqlSessionFactoryBeanName = Optional
                        .ofNullable(getBeanNameForType(SqlSessionFactory.class, listableBeanFactory));
                if (sqlSessionTemplateBeanName.isPresent() || !sqlSessionFactoryBeanName.isPresent()) {
                    builder.addPropertyValue("sqlSessionTemplateBeanName",
                            sqlSessionTemplateBeanName.orElse("sqlSessionTemplate"));
                } else {
                    builder.addPropertyValue("sqlSessionFactoryBeanName", sqlSessionFactoryBeanName.get());
                }
            }
            builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

            registry.registerBeanDefinition(configurerClass.getName(), builder.getBeanDefinition());
        }

        @Override
        public void setBeanFactory(BeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }

        @Override
        public void setEnvironment(Environment environment) {
            this.environment = environment;
        }

        private String getBeanNameForType(Class<?> type, ListableBeanFactory factory) {
            String[] beanNames = factory.getBeanNamesForType(type);
            return beanNames.length > 0 ? beanNames[0] : null;
        }
    }

    @Slf4j
    @Configuration(proxyBeanMethods = false)
    @Import(AutoConfiguredEntityScannerRegistrar.class)
    @ConditionalOnMissingBean(EntityScannerConfigurer.class)
    public static class EntityScannerRegistrarNotFoundConfiguration implements InitializingBean {

        public void afterPropertiesSet() {
            log.debug(
                    "Not found configuration for registering mapper bean using @EntityScan.");
        }
    }
}
