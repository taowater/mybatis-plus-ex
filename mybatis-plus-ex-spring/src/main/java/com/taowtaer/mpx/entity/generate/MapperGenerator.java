package com.taowtaer.mpx.entity.generate;

import cn.hutool.core.util.ReflectUtil;
import com.taowater.mpx.mapper.BaseMapper;
import com.taowater.taol.core.reflect.TypeUtil;
import com.taowtaer.mpx.entity.DynamicHelper;
import lombok.NoArgsConstructor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.aop.scope.ScopedProxyFactoryBean;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;

/**
 * Mapper 生成器
 *
 * @author zhu56
 */
@NoArgsConstructor
public class MapperGenerator extends Generator<BaseMapper<?>> {

    static final String FACTORY_BEAN_OBJECT_TYPE = "factoryBeanObjectType";

    public MapperGenerator(BeanDefinitionRegistry registry) {
        setRegistry(registry);
    }

    @Override
    public String name() {
        return "Mapper";
    }

    @Override
    public Object key(Type type) {
        return TypeUtil.getTypeArgument(type, getTargetClazz());
    }

    @Override
    public Class<? extends BaseMapper<?>> generate(Class<?> beanClass) {
        return (Class<? extends BaseMapper<?>>) DynamicHelper.buildMapper(beanClass);
    }

    @Override
    public void after(BeanDefinitionHolder holder) {
        processBeanDefinitions(holder);
    }

    /**
     * @see ClassPathMapperScanner#processBeanDefinitions(Set)
     */
    private void processBeanDefinitions(BeanDefinitionHolder holder) {
        AbstractBeanDefinition definition;
        BeanDefinitionRegistry registry = getRegistry();
        definition = (AbstractBeanDefinition) holder.getBeanDefinition();
        boolean scopedProxy = false;
        if (ScopedProxyFactoryBean.class.getName().equals(definition.getBeanClassName())) {
            definition = (AbstractBeanDefinition) Optional
                    .ofNullable(((RootBeanDefinition) definition).getDecoratedDefinition())
                    .map(BeanDefinitionHolder::getBeanDefinition).orElseThrow(() -> new IllegalStateException(
                            "The target bean definition of scoped proxy bean not found. Root bean definition[" + holder + "]"));
            scopedProxy = true;
        }
        String beanClassName = definition.getBeanClassName();

        definition.getConstructorArgumentValues().addGenericArgumentValue(beanClassName);
        try {
            Class<?> beanClass = Resources.classForName(beanClassName);
            definition.setAttribute(FACTORY_BEAN_OBJECT_TYPE, beanClass);
            definition.getPropertyValues().add("mapperInterface", beanClass);
        } catch (ClassNotFoundException ignore) {
        }

        definition.setBeanClass(MapperFactoryBean.class);

        definition.getPropertyValues().add("addToConfig", "true");

        boolean explicitFactoryUsed = false;
        String sqlSessionFactoryBeanName = (String) ReflectUtil.getFieldValue(this, "sqlSessionFactoryBeanName");
        SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) ReflectUtil.getFieldValue(this, "sqlSessionFactory");
        if (StringUtils.hasText(sqlSessionFactoryBeanName)) {
            definition.getPropertyValues().add("sqlSessionFactory",
                    new RuntimeBeanReference(sqlSessionFactoryBeanName));
            explicitFactoryUsed = true;
        } else if (sqlSessionFactory != null) {
            definition.getPropertyValues().add("sqlSessionFactory", sqlSessionFactory);
            explicitFactoryUsed = true;
        }
        String sqlSessionTemplateBeanName = (String) ReflectUtil.getFieldValue(this, "sqlSessionTemplateBeanName");
        SqlSessionTemplate sqlSessionTemplate = (SqlSessionTemplate) ReflectUtil.getFieldValue(this, "sqlSessionTemplate");
        if (StringUtils.hasText(sqlSessionTemplateBeanName)) {
            if (explicitFactoryUsed) {
            }
            definition.getPropertyValues().add("sqlSessionTemplate",
                    new RuntimeBeanReference(sqlSessionTemplateBeanName));
            explicitFactoryUsed = true;
        } else if (sqlSessionTemplate != null) {
            if (explicitFactoryUsed) {
            }
            definition.getPropertyValues().add("sqlSessionTemplate", sqlSessionTemplate);
            explicitFactoryUsed = true;
        }

        if (!explicitFactoryUsed) {
            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        }

        definition.setLazyInit(false);

        if (scopedProxy) {
            return;
        }
        String defaultScope = (String) ReflectUtil.getFieldValue(this, "defaultScope");
        if (ConfigurableBeanFactory.SCOPE_SINGLETON.equals(definition.getScope()) && defaultScope != null) {
            definition.setScope(defaultScope);
        }

        BeanDefinitionHolder finalHolder = holder;
        if (!definition.isSingleton()) {
            finalHolder = ScopedProxyUtils.createScopedProxy(holder, registry, true);
        }
        String beanName = finalHolder.getBeanName();
        if (registry.containsBeanDefinition(beanName)) {
            registry.removeBeanDefinition(beanName);
        }
        registry.registerBeanDefinition(beanName, finalHolder.getBeanDefinition());
    }


}
