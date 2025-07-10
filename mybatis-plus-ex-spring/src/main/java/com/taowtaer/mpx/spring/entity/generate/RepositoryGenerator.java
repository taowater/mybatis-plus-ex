package com.taowtaer.mpx.spring.entity.generate;

import com.taowater.taol.core.reflect.TypeUtil;
import com.taowtaer.mpx.spring.entity.GenerateHelper;
import com.taowtaer.mpx.spring.repository.DynamicRepository;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.lang.reflect.Type;

/**
 * Repository生成器
 *
 * @author zhu56
 */
public class RepositoryGenerator extends Generator<DynamicRepository<?>> {


    public RepositoryGenerator(BeanDefinitionRegistry registry) {
        setRegistry(registry);
    }

    @Override
    public String name() {
        return "Repository";
    }

    @Override
    public Object key(Type type) {
        return TypeUtil.getTypeArgument(type, getTargetClazz());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends DynamicRepository<?>> generate(Class<?> beanClass) {
        return (Class<? extends DynamicRepository<?>>) new ByteBuddy()
                .subclass(
                        parameterizedType(
                                DynamicRepository.class,
                                beanClass
                        )
                )
                .name(String.format(GenerateHelper.template, "repository", beanClass.getSimpleName(), "Repository"))
                .make()
                .load(Thread.currentThread().getContextClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                .getLoaded();
    }
}
