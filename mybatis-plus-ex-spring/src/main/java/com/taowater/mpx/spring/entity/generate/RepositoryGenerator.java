package com.taowater.mpx.spring.entity.generate;

import com.taowater.mpx.spring.entity.GenerateHelper;
import com.taowater.mpx.spring.repository.DynamicRepository;
import com.taowater.taol.core.reflect.TypeUtil;
import net.bytebuddy.ByteBuddy;
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
        return (Class<? extends DynamicRepository<?>>) GenerateHelper.loadRepository(new ByteBuddy()
                .subclass(
                        parameterizedType(
                                DynamicRepository.class,
                                beanClass
                        )
                )
                .name(GenerateHelper.className("repository", beanClass, "Repository"))
                .make());
    }
}
