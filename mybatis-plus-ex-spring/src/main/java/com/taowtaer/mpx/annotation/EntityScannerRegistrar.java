package com.taowtaer.mpx.annotation;

import com.taowater.ztream.Ztream;
import com.taowtaer.mpx.entity.EntityScannerConfigurer;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Bean definition registrar for {@link EntityScannerConfigurer}.
 *
 * @author KamToHung
 * @since 1.5.0
 */
public class EntityScannerRegistrar implements ImportBeanDefinitionRegistrar, BeanFactoryAware, EnvironmentAware {

    private BeanFactory beanFactory;
    private Environment environment;

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

    /**
     * 注册 Bean 定义
     *
     * @see org.mybatis.spring.annotation.MapperScannerRegistrar#registerBeanDefinitions(AnnotationMetadata, BeanDefinitionRegistry)
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        AnnotationAttributes dynamicAttrs = AnnotationAttributes
                .fromMap(importingClassMetadata.getAnnotationAttributes(EntityScan.class.getName()));

        registerBeanDefinitions(importingClassMetadata, dynamicAttrs, registry, generateBaseBeanName(importingClassMetadata, 0));

    }

    void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, AnnotationAttributes dynamicAttrs, BeanDefinitionRegistry registry, String beanName) {
        if (dynamicAttrs == null) {
            return;
        }

        Class<?> configurerClazz = EntityScannerConfigurer.class;
        List<String> packages = new ArrayList<>();

        packages.addAll(Ztream.of(dynamicAttrs.getStringArray("basePackages")).filter(StringUtils::hasText).toList());
        if (packages.isEmpty()) {
            packages.add(getDefaultBasePackage(importingClassMetadata));
        }
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(configurerClazz);
        builder.addPropertyValue("generators", dynamicAttrs.getClassArray("generators"));

        builder.addPropertyValue("processPropertyPlaceHolders", true);
        builder.addPropertyValue("basePackage", StringUtils.collectionToCommaDelimitedString(packages));
        BeanWrapper beanWrapper = new BeanWrapperImpl(configurerClazz);
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
        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
    }

    private static String getDefaultBasePackage(AnnotationMetadata importingClassMetadata) {
        return ClassUtils.getPackageName(importingClassMetadata.getClassName());
    }


    private static String generateBaseBeanName(AnnotationMetadata importingClassMetadata, int index) {
        return importingClassMetadata.getClassName() + "#" + EntityScannerRegistrar.class.getSimpleName() + "#" + index;
    }

    /**
     * A {@link EntityScannerRegistrar} for {@link EntityScans}.
     *
     * @since 2.0.0
     */
    static class RepeatingRegistrar extends EntityScannerRegistrar {
        /**
         * {@inheritDoc}
         */
        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            AnnotationAttributes mapperScansAttrs = AnnotationAttributes
                    .fromMap(importingClassMetadata.getAnnotationAttributes(EntityScans.class.getName()));
            if (mapperScansAttrs != null) {
                AnnotationAttributes[] annotations = mapperScansAttrs.getAnnotationArray("value");
                for (int i = 0; i < annotations.length; i++) {
                    registerBeanDefinitions(importingClassMetadata, annotations[i], registry,
                            generateBaseBeanName(importingClassMetadata, i));
                }
            }
        }
    }
}
