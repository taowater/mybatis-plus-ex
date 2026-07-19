package com.taowater.mpx.spring.entity;

import com.taowater.ztream.Ztream;
import lombok.experimental.UtilityClass;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Bean定义相关工具
 *
 * @author zhu56
 * @date 2025/04/26 20:18
 */
@UtilityClass
public class BeanDefinitionUtil {

    private static final Log log = LogFactory.getLog(BeanDefinitionUtil.class);

    public static List<Type> find(BeanDefinitionRegistry registry, Class<?> clazz) {
        if (registry instanceof ListableBeanFactory) {
            ListableBeanFactory listableBeanFactory = (ListableBeanFactory) registry;
            return Ztream.of(listableBeanFactory.getBeanNamesForType(ResolvableType.forClass(clazz)))
                    .map(e -> getBeanResolvableType(registry, e))
                    .nonNull()
                    .toList(ResolvableType::getType);
        }
        return Collections.emptyList();

    }

    private static ResolvableType getBeanResolvableType(BeanDefinitionRegistry registry, String beanName) {
        try {
            if (registry.getBeanDefinition(beanName) instanceof AbstractBeanDefinition) {
                BeanDefinition abd = registry.getBeanDefinition(beanName);
                ResolvableType type = abd.getResolvableType();
                if (type != ResolvableType.NONE) {
                    boolean isFactoryBean = FactoryBean.class.isAssignableFrom(Objects.requireNonNull(type.getRawClass()));
                    if (!isFactoryBean) {
                        return type;
                    }
                }
                Class<?> beanClass = ((ListableBeanFactory) registry).getType(beanName);
                if (beanClass != null) {
                    return ResolvableType.forClass(beanClass);
                }
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to resolve ResolvableType for bean '" + beanName + "': " + e.getMessage(), e);
            }
        }
        return null;
    }
}
