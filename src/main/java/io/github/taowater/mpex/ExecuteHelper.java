package io.github.taowater.mpex;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import io.github.taowater.core.function.LambdaUtil;
import io.github.taowater.core.function.SerBiFunction;
import io.github.taowater.util.EmptyUtil;
import io.github.taowater.ztream.Any;
import lombok.experimental.UtilityClass;
import lombok.var;
import org.dromara.hutool.core.reflect.TypeUtil;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 执行帮助程序
 *
 * @author zhu56
 * @date 2024/06/14 01:29
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
     * @return {@link R} 业务查询结果
     */
    @SuppressWarnings("unchecked")
    public static <T, M extends BaseMapper<T>, W extends Wrapper<T>, R> R execute(M mapper, Consumer<W> operator, SerBiFunction<M, W, R> fun, Function<Class<T>, W> wFun) {
        if (EmptyUtil.isHadEmpty(mapper, operator, fun, wFun)) {
            return null;
        }
        // 获得该mapper操作的实体类型
        Class<T> clazz = (Class<T>) TypeUtil.getTypeArgument(mapper.getClass(), 0);
        // 获取wrapper对象
        W wrapper = wFun.apply(clazz);
        // 使用操作流程描述处理该wrapper得到最终查询的wrapper
        operator.accept(wrapper);
        //如果是拓展的wrapper类型
        if (wrapper instanceof AbstractLambdaExWrapper<?, ?>) {
            var w = (AbstractLambdaExWrapper<?, ?>) wrapper;
            //如果无需查库则返回对应类型的默认值
            if (!Any.of(w).get(AbstractLambdaExWrapper::getNeedQuery, true)) {
                Class<R> returnClazz = LambdaUtil.getReturnClass(fun);
                return DefaultHelper.getValue(returnClazz);
            }
        }
        // 执行查询并返回结果
        return fun.apply(mapper, wrapper);
    }
}
