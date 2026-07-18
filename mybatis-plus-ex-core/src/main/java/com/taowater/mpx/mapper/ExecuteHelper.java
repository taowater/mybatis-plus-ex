package com.taowater.mpx.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.override.MybatisMapperProxy;
import com.baomidou.mybatisplus.core.toolkit.MybatisUtils;
import com.taowater.mpx.wrapper.interfaces.CompareRequired;
import com.taowater.taol.core.function.Function2;
import com.taowater.taol.core.reflect.TypeUtil;
import lombok.experimental.UtilityClass;
import lombok.var;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 执行帮助程序
 *
 * @author zhu56
 */
@UtilityClass
class ExecuteHelper {

    /**
     * mapper层执行快查处理
     *
     * @param mapper   执行的mapper
     * @param operator wrapper处理过程
     * @param fun      mapper执行的业务查询
     * @param wFun     wrapper类型的获取方法
     */
    @SuppressWarnings("unchecked")
    public static <T, M extends BaseMapper<T>, W extends Wrapper<T>, R> R execute(M mapper, Consumer<W> operator, Function2<M, W, R> fun, Function<Class<T>, W> wFun) {

        Objects.requireNonNull(mapper, "mapper");
        Objects.requireNonNull(fun, "fun");
        Objects.requireNonNull(wFun, "wFun");

        // 获得该mapper操作的实体类型（优先从 Mapper 接口泛型解析，避免 JDK/CGLIB 代理干扰）
        Class<T> clazz = resolveEntityClass(mapper);
        // 获取wrapper对象
        W wrapper = wFun.apply(clazz);
        // 使用操作流程描述处理该wrapper得到最终查询的wrapper
        if (Objects.nonNull(operator)) {
            operator.accept(wrapper);
        }
        //如果是拓展的wrapper类型
        if (wrapper instanceof CompareRequired<?, ?>) {
            var w = (CompareRequired<?, ?>) wrapper;
            //如果无需查库则返回对应类型的默认值
            if (!w.needExecute()) {
                return null;
            }
        }
        // 执行查询并返回结果
        return fun.apply(mapper, wrapper);
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> resolveEntityClass(BaseMapper<T> mapper) {
        try {
            MybatisMapperProxy<?> proxy = MybatisUtils.getMybatisMapperProxy(mapper);
            Class<?> mapperInterface = proxy.getMapperInterface();
            Class<T> entity = (Class<T>) TypeUtil.getTypeArgument(mapperInterface, BaseMapper.class);
            if (entity != null) {
                return entity;
            }
        } catch (Throwable ignored) {
            // 非代理或无法解析时回退
        }
        Class<T> entity = (Class<T>) TypeUtil.getTypeArgument(mapper.getClass(), BaseMapper.class);
        if (entity != null) {
            return entity;
        }
        for (Class<?> iface : mapper.getClass().getInterfaces()) {
            if (BaseMapper.class.isAssignableFrom(iface)) {
                entity = (Class<T>) TypeUtil.getTypeArgument(iface, BaseMapper.class);
                if (entity != null) {
                    return entity;
                }
            }
        }
        throw new IllegalStateException("Cannot resolve entity type from mapper: " + mapper.getClass().getName());
    }
}
