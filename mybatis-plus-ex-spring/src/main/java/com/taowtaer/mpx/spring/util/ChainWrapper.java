package com.taowtaer.mpx.spring.util;

import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.taowater.mpx.mapper.BaseMapper;
import com.taowater.mpx.wrapper.chain.LambdaQueryChainExWrapper;
import com.taowater.mpx.wrapper.chain.LambdaUpdateChainExWrapper;
import com.taowater.mpx.wrapper.chain.QueryChainExWrapper;
import com.taowater.mpx.wrapper.chain.UpdateChainExWrapper;
import lombok.experimental.UtilityClass;

/**
 * 快捷构造拓展链式调用工具类
 *
 * @see ChainWrappers
 */
@UtilityClass
public class ChainWrapper {


    /**
     * 链式查询 普通
     *
     * @return QueryWrapper 的包装类
     */
    public static <T> QueryChainExWrapper<T> queryChain(BaseMapper<T> mapper) {
        return new QueryChainExWrapper<>(mapper);
    }

    public static <T> QueryChainExWrapper<T> queryChain(Class<T> entityClass) {
        return new QueryChainExWrapper<>(entityClass);
    }

    /**
     * 链式查询 lambda 式
     * <p>注意：不支持 Kotlin </p>
     *
     * @return LambdaQueryWrapper 的包装类
     */
    public static <T> LambdaQueryChainExWrapper<T> lambdaQueryChain(BaseMapper<T> mapper) {
        return new LambdaQueryChainExWrapper<>(mapper);
    }

    public static <T> LambdaQueryChainExWrapper<T> lambdaQueryChain(Class<T> entityClass) {
        return new LambdaQueryChainExWrapper<>(entityClass);
    }

    /**
     * 链式查询 lambda 式
     * <p>注意：不支持 Kotlin </p>
     *
     * @return LambdaQueryWrapper 的包装类
     */
    public static <T> LambdaQueryChainExWrapper<T> lambdaQueryChain(BaseMapper<T> mapper, T entity) {
        return new LambdaQueryChainExWrapper<>(mapper, entity);
    }

    /**
     * 链式查询 lambda 式
     * <p>注意：不支持 Kotlin </p>
     *
     * @return LambdaQueryWrapper 的包装类
     */
    public static <T> LambdaQueryChainExWrapper<T> lambdaQueryChain(BaseMapper<T> mapper, Class<T> entityClass) {
        return new LambdaQueryChainExWrapper<>(mapper, entityClass);
    }


    /**
     * 链式更改 普通
     *
     * @return UpdateWrapper 的包装类
     */
    public static <T> UpdateChainExWrapper<T> updateChain(BaseMapper<T> mapper) {
        return new UpdateChainExWrapper<>(mapper);
    }

    public static <T> UpdateChainExWrapper<T> updateChain(Class<T> entityClass) {
        return new UpdateChainExWrapper<>(entityClass);
    }

    /**
     * 链式更改 lambda 式
     * <p>注意：不支持 Kotlin </p>
     *
     * @return LambdaUpdateWrapper 的包装类
     */
    public static <T> LambdaUpdateChainExWrapper<T> lambdaUpdateChain(BaseMapper<T> mapper) {
        return new LambdaUpdateChainExWrapper<>(mapper);
    }

    public static <T> LambdaUpdateChainExWrapper<T> lambdaUpdateChain(Class<T> entityClass) {
        return new LambdaUpdateChainExWrapper<>(entityClass);
    }
}
