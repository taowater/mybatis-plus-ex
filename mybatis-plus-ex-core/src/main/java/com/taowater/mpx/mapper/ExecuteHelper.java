package com.taowater.mpx.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.taowater.mpx.wrapper.interfaces.CompareRequired;
import com.taowater.taol.core.function.Function2;
import com.taowater.taol.core.function.LambdaUtil;
import com.taowater.taol.core.reflect.TypeUtil;
import com.taowater.taol.core.util.EmptyUtil;
import com.taowater.ztream.Any;
import lombok.experimental.UtilityClass;
import lombok.var;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 执行帮助程序
 *
 * @author zhu56
 */
@UtilityClass
class ExecuteHelper {

    private final static Map<String, String> MS_CACHE = new ConcurrentHashMap<>();

    /**
     * mapper层执行快查处理
     *
     * @param mapper   执行的mapper
     * @param operator wrapper处理过程
     * @param fun      mapper执行的业务查询
     * @param wFun     wrapper类型的获取方法
     * @return {@link R} 业务查询结果
     */
    @SuppressWarnings("unchecked")
    public static <T, M extends BaseMapper<T>, W extends Wrapper<T>, R> R execute(M mapper, Consumer<W> operator, Function2<M, W, R> fun, Function<Class<T>, W> wFun) {
        if (EmptyUtil.isHadEmpty(mapper, operator, fun, wFun)) {
            return null;
        }
        // 获得该mapper操作的实体类型
        Class<T> clazz = (Class<T>) TypeUtil.getTypeArgument(mapper.getClass(), BaseMapper.class);
        // 获取wrapper对象
        W wrapper = wFun.apply(clazz);
        // 使用操作流程描述处理该wrapper得到最终查询的wrapper
        operator.accept(wrapper);
        //如果是拓展的wrapper类型
        if (wrapper instanceof CompareRequired<?, ?>) {
            var w = (CompareRequired<?, ?>) wrapper;
            //如果无需查库则返回对应类型的默认值
            if (!Any.of(w).get(CompareRequired::needExecute, true)) {
                Class<R> returnClazz = LambdaUtil.getReturnClass(fun);
                return DefaultHelper.getValue(returnClazz);
            }
        }
        // 执行查询并返回结果
        return fun.apply(mapper, wrapper);
    }

    /**
     * 构建动态 MappedStatement (带缓存自动生效)
     */
    public static <D, M extends BaseMapper<?>> String buildDynamicMappedStatement(Configuration config, SqlMethod sqlMethod, Class<D> clazz, Class<M> mapperClazz) {
        // 获取BaseMapper的原始selectList语句
        String originalMsId = SqlHelper.getSqlStatement(mapperClazz, sqlMethod);
        String dynamicMsId = originalMsId + "_" + clazz.getName();
        return MS_CACHE.computeIfAbsent(dynamicMsId, k -> {
            // 检查是否已注册（防止其他线程抢先注册）
            if (config.hasStatement(k, false)) {
                return k;
            }
            MappedStatement originalMs = config.getMappedStatement(originalMsId);
            // 创建唯一ID（使用类全限定名+方法名+目标类型）
            MappedStatement ms = new MappedStatement.Builder(config, k, originalMs.getSqlSource(), SqlCommandType.SELECT)
                    .resultMaps(Collections.singletonList(
                            new ResultMap.Builder(config, k + "_", clazz, Collections.emptyList()).build()
                    ))
                    .build();
            config.addMappedStatement(ms);
            return k;
        });
    }
}
