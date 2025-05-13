package com.taowtaer.mpx.spring.entity;

import com.taowater.ztream.Ztream;
import lombok.experimental.UtilityClass;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Type;
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

    public static List<Type> find(BeanDefinitionRegistry registry, Class<?> clazz) {
        if (registry instanceof ListableBeanFactory) {
            ListableBeanFactory listableBeanFactory = (ListableBeanFactory) registry;
            return Ztream.of(listableBeanFactory.getBeanNamesForType(ResolvableType.forClass(clazz)))
                    .map(e -> getBeanResolvableType(registry, e))
                    .nonNull()
                    .toList(ResolvableType::getType);
        }
        return null;

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
            // 处理异常
        }
        return null;
    }
}
