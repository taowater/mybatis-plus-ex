package com.taowater.mpx.spring.entity.generate;

import com.taowater.mpx.mapper.BaseMapper;
import com.taowater.mpx.spring.entity.GenerateHelper;
import com.taowater.taol.core.reflect.TypeUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bytebuddy.ByteBuddy;
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

    @Getter
    @Setter
    private String sqlSessionFactoryBeanName;
    @Getter
    @Setter
    private SqlSessionFactory sqlSessionFactory;
    @Getter
    @Setter
    private String sqlSessionTemplateBeanName;
    @Getter
    @Setter
    private SqlSessionTemplate sqlSessionTemplate;
    @Getter
    @Setter
    private String defaultScope;

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
    @SuppressWarnings("unchecked")
    public Class<? extends BaseMapper<?>> generate(Class<?> beanClass) {
        return (Class<? extends BaseMapper<?>>) GenerateHelper.loadMapper(new ByteBuddy()
                .makeInterface(parameterizedType(BaseMapper.class, beanClass))
                .name(GenerateHelper.className("mapper", beanClass, "Mapper"))
                .make());
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
        // WRAPPER 策略下类在子 ClassLoader，不能再按类名 Class.forName；优先用 BeanDefinition 上已有的 Class
        Class<?> mapperInterface;
        if (definition.hasBeanClass()) {
            mapperInterface = definition.getBeanClass();
        } else {
            try {
                mapperInterface = Resources.classForName(beanClassName);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Cannot resolve mapper interface: " + beanClassName, e);
            }
        }
        definition.getConstructorArgumentValues().addGenericArgumentValue(mapperInterface);
        definition.setAttribute(FACTORY_BEAN_OBJECT_TYPE, mapperInterface);
        definition.getPropertyValues().add("mapperInterface", mapperInterface);

        definition.setBeanClass(MapperFactoryBean.class);

        definition.getPropertyValues().add("addToConfig", "true");

        boolean explicitFactoryUsed = false;
        if (StringUtils.hasText(sqlSessionFactoryBeanName)) {
            definition.getPropertyValues().add("sqlSessionFactory",
                    new RuntimeBeanReference(sqlSessionFactoryBeanName));
            explicitFactoryUsed = true;
        } else if (sqlSessionFactory != null) {
            definition.getPropertyValues().add("sqlSessionFactory", sqlSessionFactory);
            explicitFactoryUsed = true;
        }
        if (StringUtils.hasText(sqlSessionTemplateBeanName)) {
            definition.getPropertyValues().add("sqlSessionTemplate",
                    new RuntimeBeanReference(sqlSessionTemplateBeanName));
            explicitFactoryUsed = true;
        } else if (sqlSessionTemplate != null) {
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
        if (ConfigurableBeanFactory.SCOPE_SINGLETON.equals(definition.getScope()) && StringUtils.hasText(defaultScope)) {
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
