package com.taowater.mpex.dynamic;

import com.taowater.mpex.BaseMapper;
import lombok.experimental.UtilityClass;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

import java.lang.reflect.Type;

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

//    public static Class<?> buildService(Class<?> controllerClazz) {
//
//        Class<?> entityClass = (Class<?>) TypeUtil.getTypeArgument(controllerClazz, BaseCrudController.class, 0);
//        Class<?> voClass = (Class<?>) TypeUtil.getTypeArgument(controllerClazz, BaseCrudController.class, 1);
//        Class<?> paramClass = (Class<?>) TypeUtil.getTypeArgument(controllerClazz, BaseCrudController.class, 2);
//        return
//                new ByteBuddy()
//                        .subclass(
//                                parameterizedType(
//                                        BaseAdminServiceImpl.class,
//                                        entityClass,
//                                        voClass,
//                                        paramClass
//                                )
//                        )
//                        .name(String.format(template, "service", entityClass.getSimpleName(), "Service"))
//                        .make()
//                        .load(Thread.currentThread().getContextClassLoader(), ClassLoadingStrategy.Default.INJECTION)
//                        .getLoaded();
//    }
}
