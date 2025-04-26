package com.taowtaer.mpx.entity.generate;

import com.taowater.taol.core.reflect.TypeUtil;
import com.taowater.ztream.Ztream;
import com.taowtaer.mpx.entity.BeanDefinitionUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 生成器
 *
 * @author zhu56
 * @date 2025/04/27 00:21
 */
public abstract class Generator<T> {

    @Setter
    @Getter
    private BeanDefinitionRegistry registry;

    private Map<Object, Type> map;

    private Log getLog() {
        return LogFactory.getLog(this.getClass());
    }

    protected BeanNameGenerator beanNameGenerator = AnnotationBeanNameGenerator.INSTANCE;

    @SuppressWarnings("unchecked")
    protected Class<T> getTargetClazz() {
        Type type = TypeUtil.getTypeArgument(getClass(), Generator.class);
        if (type instanceof Class) {
            return (Class<T>) type;
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return (Class<T>) parameterizedType.getRawType();
        }
        return null;
    }


    /**
     * 从注册器寻找目标类已有的bean定义
     *
     * @param clazz 克拉兹
     * @return {@link List }<{@link Type }>
     */
    protected List<Type> find(BeanDefinitionRegistry registry, Class<T> clazz) {
        return BeanDefinitionUtil.find(registry, clazz);
    }

    /**
     * 此组件名字
     *
     * @return {@link String }
     */
    protected abstract String name();

    /**
     * 此组件判定唯一视为已经创建的逻辑
     */
    protected abstract Object key(Type type);

    /**
     * 此类创建真实类的逻辑
     */
    protected abstract Class<?> generate(Class<?> beanClass);


    protected BeanDefinitionHolder simpleBdHolder(Class<?> clazz) {
        BeanDefinition bd = BeanDefinitionBuilder.genericBeanDefinition(clazz).getBeanDefinition();
        return new BeanDefinitionHolder(bd, beanNameGenerator.generateBeanName(bd, getRegistry()));
    }

    /**
     * 得到bean定义的处理逻辑
     * 默认为将其注册
     *
     * @param holder 持有
     */
    protected void after(BeanDefinitionHolder holder) {
        getRegistry().registerBeanDefinition(holder.getBeanName(), holder.getBeanDefinition());
    }

    protected Map<Object, Type> map() {
        if (Objects.isNull(map)) {
            List<Type> types = find(getRegistry(), getTargetClazz());
            map = Ztream.of(types).toMap(this::key);
        }
        return map;
    }

    public final void handle(Class<?> beanClazz) {

        if (!map().containsKey(beanClazz)) {
            Class<?> generated = generate(beanClazz);
            if (Objects.isNull(generated)) {
                return;
            }
            BeanDefinitionHolder holder = simpleBdHolder(generated);
            after(holder);
            getLog().info("Generate " + name() + " for " + beanClazz.getName());
        }
    }
}
