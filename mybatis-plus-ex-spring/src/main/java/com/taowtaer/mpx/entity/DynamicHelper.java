package com.taowtaer.mpx.entity;

import com.taowater.mpx.mapper.BaseMapper;
import com.taowtaer.mpx.repository.DynamicRepository;
import lombok.experimental.UtilityClass;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

import java.lang.reflect.Type;

/**
 * 动态生成
 *
 * @author zhu56
 * @date 2025/04/26 00:18
 */
@UtilityClass
public class DynamicHelper {

    private final static String template = "dynamic.%s.%sDynamic%s";


    private static TypeDescription.Generic parameterizedType(Class<?> rawType, Type... parameter) {
        return TypeDescription.Generic.Builder.parameterizedType(rawType, parameter)
                .build();
    }

    public static Class<?> buildMapper(Class<?> clazz) {
        return new ByteBuddy()
                .makeInterface(parameterizedType(BaseMapper.class, clazz))
                .name(String.format(template, "mapper", clazz.getSimpleName(), "Mapper"))
                .make()
                .load(Thread.currentThread().getContextClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                .getLoaded();
    }

    public static Class<?> buildRepository(Class<?> entityClass) {
        return new ByteBuddy()
                .subclass(
                        parameterizedType(
                                DynamicRepository.class,
                                entityClass
                        )
                )
                .name(String.format(template, "repository", entityClass.getSimpleName(), "Repository"))
                .make()
                .load(Thread.currentThread().getContextClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                .getLoaded();
    }
}
