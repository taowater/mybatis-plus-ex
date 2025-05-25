package com.taowater.mpx.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.taowater.mpx.wrapper.LambdaQueryExWrapper;
import com.taowater.mpx.wrapper.LambdaUpdateExWrapper;
import com.taowater.taol.core.convert.ConvertUtil;
import com.taowater.taol.core.reflect.TypeUtil;
import com.taowater.taol.core.util.EmptyUtil;
import com.taowater.ztream.Any;
import com.taowater.ztream.Ztream;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.apache.ibatis.session.SqlSession;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 基础mapper
 *
 * @author zhu56
 */
@SuppressWarnings({"unchecked", "unused"})
public interface BaseMapper<T> extends com.baomidou.mybatisplus.core.mapper.BaseMapper<T> {

    /**
     * 查询总记录数
     */
    default Long selectCount() {
        return this.selectCount((Wrapper<T>) null);
    }

    /**
     * 查询总记录数
     *
     * @param consumer 操作符
     */
    default Long selectCount(Consumer<LambdaQueryExWrapper<T>> consumer) {
        Long result = ExecuteHelper.execute(this, consumer, BaseMapper::selectCount, LambdaQueryExWrapper::new);
        return Any.of(result).orElse(0L);
    }


    /**
     * 查询列表
     */
    default List<T> selectList() {
        return this.selectList((Wrapper<T>) null);
    }

    /**
     * 查询列表
     *
     * @param consumer 操作符
     */
    default List<T> selectList(Consumer<LambdaQueryExWrapper<T>> consumer) {
        List<T> result = ExecuteHelper.execute(this, consumer, BaseMapper::selectList, LambdaQueryExWrapper::new);
        return Any.of(result).orElse(new ArrayList<>(0));
    }

    /**
     * 分页查询
     *
     * @param page     分页参数
     * @param consumer 操作符
     */
    default <P extends IPage<T>> P selectPage(P page, Consumer<LambdaQueryExWrapper<T>> consumer) {
        P result = ExecuteHelper.execute(this, consumer, (m, w) -> m.selectPage(page, w), LambdaQueryExWrapper::new);
        return Any.of(result).orElse(page);
    }

    /**
     * 查询为流
     */
    default Ztream<T> selectZtream() {
        return selectZtream(null);
    }

    /**
     * 查询为流
     *
     * @param consumer 操作
     * @return {@link Ztream}<{@link T}>
     */
    default Ztream<T> selectZtream(Consumer<LambdaQueryExWrapper<T>> consumer) {
        return Ztream.of(selectList(consumer));
    }

    /**
     * 查询一个
     *
     * @param consumer 操作符
     */
    default T selectOne(Consumer<LambdaQueryExWrapper<T>> consumer) {
        return selectOne(consumer, false);
    }

    /**
     * 查询一个，结果用Any包装
     *
     * @param consumer 操作符
     */
    default Any<T> selectOneX(Consumer<LambdaQueryExWrapper<T>> consumer) {
        return Any.of(selectOne(consumer));
    }

    /**
     * 判断是否存在
     *
     * @param consumer 操作
     */
    default boolean exists(Consumer<LambdaQueryExWrapper<T>> consumer) {
        Integer result = ExecuteHelper.execute(this, consumer, BaseMapper::selectExists, LambdaQueryExWrapper::new);
        return Objects.nonNull(result);
    }

    /**
     * 判断数据是否存在
     *
     * @param queryWrapper 查询包装器
     */
    Integer selectExists(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);

    /**
     * 查询一个
     *
     * @param consumer 操作
     * @param throwEx  结果多个是否抛出异常
     */
    default T selectOne(Consumer<LambdaQueryExWrapper<T>> consumer, boolean throwEx) {
        consumer = consumer.andThen(w -> w.limit(throwEx ? 2 : 1));
        List<T> list = this.selectList(consumer);
        if (EmptyUtil.isEmpty(list)) {
            return null;
        }
        if (throwEx && list.size() > 1) {
            throw new TooManyResultsException("Expected one result (or null) to be returned by selectOne(), but more");
        }
        return list.get(0);
    }

    /**
     * 更新操作
     *
     * @param e        实体
     * @param consumer 操作
     */
    default int update(T e, Consumer<LambdaUpdateExWrapper<T>> consumer) {
        Integer update = ExecuteHelper.execute(this, consumer, (m, w) -> m.update(e, w), LambdaUpdateExWrapper::new);
        return Any.of(update).orElse(0);
    }

    /**
     * 更新操作
     *
     * @param consumer 操作
     */
    default int update(Consumer<LambdaUpdateExWrapper<T>> consumer) {
        return update(null, consumer);
    }

    /**
     * 删除操作
     *
     * @param consumer 操作符
     */
    default int delete(Consumer<LambdaQueryExWrapper<T>> consumer) {
        Integer result = ExecuteHelper.execute(this, consumer, BaseMapper::delete, LambdaQueryExWrapper::new);
        return Any.of(result).orElse(0);
    }

    /**
     * 指定属性查询并按该属性分组
     *
     * @param field  属性
     * @param values 值集合
     */
    default <K extends Serializable> Map<K, List<T>> groupBy(SFunction<T, K> field, Collection<K> values) {
        return this.groupBy(field, values, Function.identity());
    }

    /**
     * 指定属性查询并按该属性分组,按指定方法处理得到分组值
     *
     * @param field  属性
     * @param values 值集合
     * @param funV   分组值处理方法
     */
    default <K extends Serializable, A> Map<K, List<A>> groupBy(SFunction<T, K> field, Collection<K> values, Function<T, A> funV) {
        if (EmptyUtil.isHadEmpty(field, values)) {
            return new HashMap<>(0);
        }
        return selectZtream(w -> w.in(field, values)).groupBy(field, funV);
    }

    /**
     * 指定属性查询并按该属性分组,按转换类型得到分组值
     *
     * @param field  属性
     * @param values 值集合
     * @param clazzV 分组值转换类型
     */
    default <K extends Serializable, A> Map<K, List<A>> groupBy(SFunction<T, K> field, Collection<K> values, Class<A> clazzV) {
        return groupBy(field, values, e -> ConvertUtil.convert(e, clazzV));
    }


    /**
     * 指定属性查询得到[该属性为key，对应数据为value的map]
     *
     * @param field  属性
     * @param values 值集合
     */
    default <K extends Serializable> Map<K, T> mapBy(SFunction<T, K> field, Collection<K> values) {
        return mapBy(field, values, Function.identity());
    }


    /**
     * 指定属性查询得到[该属性为key，指定方法处理后的值为value的map]
     *
     * @param field  属性
     * @param values 值集合
     * @param funV   值处理方法
     */
    default <K extends Serializable, A> Map<K, A> mapBy(SFunction<T, K> field, Collection<K> values, Function<T, A> funV) {
        if (EmptyUtil.isHadEmpty(field, values)) {
            return new HashMap<>(0);
        }
        return selectZtream(w -> w.in(field, values)).toMap(field, funV);
    }

    /**
     * 指定属性查询得到[该属性为key，指定方法处理后的值为value的map]
     *
     * @param field  属性
     * @param values 值集合
     * @param clazzV 值转换类型
     */
    default <K extends Serializable, A> Map<K, A> mapBy(SFunction<T, K> field, Collection<K> values, Class<A> clazzV) {
        return mapBy(field, values, e -> ConvertUtil.convert(e, clazzV));
    }

    default <D> List<D> selectList(Wrapper<T> wrapper, Class<D> clazz) {
        // 获取原始 Mapper 接口 Class（解决代理问题）
        Class<T> entityClazz = (Class<T>) TypeUtil.getTypeArgument(this.getClass(), BaseMapper.class);

        Class<? extends BaseMapper<T>> mapperInterface = (Class<? extends BaseMapper<T>>) Ztream.of(this.getClass().getInterfaces()).filter(BaseMapper.class::isAssignableFrom).getFirst();
        SqlSession sqlSession = SqlHelper.sqlSession(entityClazz);
        // 从缓存获取或构建 MappedStatement
        String msId = ExecuteHelper.buildDynamicMappedStatement(sqlSession.getConfiguration(), SqlMethod.SELECT_LIST, clazz, mapperInterface);

        return sqlSession.selectList(msId, Collections.singletonMap(Constants.WRAPPER, wrapper));
    }

    default <D> List<D> selectList(Consumer<LambdaQueryExWrapper<T>> consumer, Class<D> clazz) {
        return ExecuteHelper.execute(this, consumer, (m, w) -> m.selectList(w, clazz), LambdaQueryExWrapper::new);
    }
}
