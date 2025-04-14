package com.taowater.mpex;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.taowater.mpex.wrapper.LambdaQueryExWrapper;
import com.taowater.mpex.wrapper.LambdaUpdateExWrapper;
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
@SuppressWarnings("unchecked")
public interface BaseMapper<P> extends com.baomidou.mybatisplus.core.mapper.BaseMapper<P> {

    /**
     * 查询总记录数
     *
     * @return {@link Long }
     */
    default Long selectCount() {
        return this.selectCount((Wrapper<P>) null);
    }

    /**
     * 查询总记录数
     *
     * @param consumer 操作符
     * @return {@link Long}
     */
    default Long selectCount(Consumer<LambdaQueryExWrapper<P>> consumer) {
        Long result = ExecuteHelper.execute(this, consumer, BaseMapper::selectCount, LambdaQueryExWrapper::new);
        return Any.of(result).orElse(0L);
    }


    /**
     * 查询列表
     *
     * @return {@link List }<{@link P }>
     */
    default List<P> selectList() {
        return this.selectList((Wrapper<P>) null);
    }

    /**
     * 查询列表
     *
     * @param consumer 操作符
     * @return {@link List}<{@link P}>
     */
    default List<P> selectList(Consumer<LambdaQueryExWrapper<P>> consumer) {
        List<P> result = ExecuteHelper.execute(this, consumer, BaseMapper::selectList, LambdaQueryExWrapper::new);
        return Any.of(result).orElse(new ArrayList<>(0));
    }

    /**
     * 分页查询
     *
     * @param page     分页参数
     * @param consumer 操作符
     * @return {@link Page}<{@link P}>
     */
    default <E extends IPage<P>> E selectPage(E page, Consumer<LambdaQueryExWrapper<P>> consumer) {
        E result = ExecuteHelper.execute(this, consumer, (m, w) -> m.selectPage(page, w), LambdaQueryExWrapper::new);
        return Any.of(result).orElse(page);
    }

    /**
     * 查询为流
     *
     * @return {@link Ztream }<{@link P }>
     */
    default Ztream<P> selectZtream() {
        return Ztream.of(selectList());
    }

    /**
     * 查询为流
     *
     * @param consumer 操作
     * @return {@link Ztream}<{@link P}>
     */
    default Ztream<P> selectZtream(Consumer<LambdaQueryExWrapper<P>> consumer) {
        return Ztream.of(selectList(consumer));
    }

    /**
     * 查询一个
     *
     * @param consumer 操作符
     * @return {@link P}
     */
    default P selectOne(Consumer<LambdaQueryExWrapper<P>> consumer) {
        return selectOne(consumer, false);
    }

    /**
     * 查询一个，结果用Any包装
     *
     * @param consumer 操作符
     * @return {@link Any}<{@link P}>
     */
    default Any<P> selectOneX(Consumer<LambdaQueryExWrapper<P>> consumer) {
        return Any.of(selectOne(consumer));
    }

    /**
     * 判断是否存在
     *
     * @param consumer 操作
     * @return boolean
     */
    default boolean exists(Consumer<LambdaQueryExWrapper<P>> consumer) {
        Integer result = ExecuteHelper.execute(this, consumer, BaseMapper::selectExists, LambdaQueryExWrapper::new);
        return Objects.nonNull(result);
    }

    /**
     * 判断数据是否存在
     *
     * @param queryWrapper 查询包装器
     * @return {@link Integer }
     */
    Integer selectExists(@Param(Constants.WRAPPER) Wrapper<P> queryWrapper);

    /**
     * 查询一个
     *
     * @param consumer 操作
     * @param throwEx  结果多个是否抛出异常
     * @return {@link P}
     */
    default P selectOne(Consumer<LambdaQueryExWrapper<P>> consumer, boolean throwEx) {
        consumer = consumer.andThen(w -> w.limit(throwEx ? 2 : 1));
        List<P> list = this.selectList(consumer);
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
     * @return int 更新数量
     */
    default int update(P e, Consumer<LambdaUpdateExWrapper<P>> consumer) {
        Integer update = ExecuteHelper.execute(this, consumer, (m, w) -> m.update(e, w), LambdaUpdateExWrapper::new);
        return Any.of(update).orElse(0);
    }

    /**
     * 更新操作
     *
     * @param consumer 操作
     * @return int 更新数量
     */
    default int update(Consumer<LambdaUpdateExWrapper<P>> consumer) {
        return update(null, consumer);
    }

    /**
     * 删除操作
     *
     * @param consumer 操作符
     * @return int 删除数量
     */
    default int delete(Consumer<LambdaQueryExWrapper<P>> consumer) {
        Integer result = ExecuteHelper.execute(this, consumer, BaseMapper::delete, LambdaQueryExWrapper::new);
        return Any.of(result).orElse(0);
    }

    /**
     * 指定属性查询并按该属性分组
     *
     * @param field  属性
     * @param values 值集合
     * @return {@link Map}<{@link K}, {@link List}<{@link P}>>
     */
    default <K extends Serializable> Map<K, List<P>> groupBy(SFunction<P, K> field, Collection<K> values) {
        return this.groupBy(field, values, Function.identity());
    }

    /**
     * 指定属性查询并按该属性分组,按指定方法处理得到分组值
     *
     * @param field  属性
     * @param values 值集合
     * @param funV   分组值处理方法
     * @return {@link Map}<{@link K}, {@link List}<{@link A}>>
     */
    default <K extends Serializable, A> Map<K, List<A>> groupBy(SFunction<P, K> field, Collection<K> values, Function<P, A> funV) {
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
     * @return {@link Map}<{@link K}, {@link List}<{@link A}>>
     */
    default <K extends Serializable, A> Map<K, List<A>> groupBy(SFunction<P, K> field, Collection<K> values, Class<A> clazzV) {
        return groupBy(field, values, e -> ConvertUtil.convert(e, clazzV));
    }


    /**
     * 指定属性查询得到[该属性为key，对应数据为value的map]
     *
     * @param field  属性
     * @param values 值集合
     * @return {@link Map}<{@link K}, {@link P}>
     */
    default <K extends Serializable> Map<K, P> mapBy(SFunction<P, K> field, Collection<K> values) {
        return mapBy(field, values, Function.identity());
    }


    /**
     * 指定属性查询得到[该属性为key，指定方法处理后的值为value的map]
     *
     * @param field  属性
     * @param values 值集合
     * @param funV   值处理方法
     * @return {@link Map}<{@link K}, {@link A}>
     */
    default <K extends Serializable, A> Map<K, A> mapBy(SFunction<P, K> field, Collection<K> values, Function<P, A> funV) {
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
     * @return {@link Map}<{@link K}, {@link A}>
     */
    default <K extends Serializable, A> Map<K, A> mapBy(SFunction<P, K> field, Collection<K> values, Class<A> clazzV) {
        return mapBy(field, values, e -> ConvertUtil.convert(e, clazzV));
    }

    default <D> List<D> selectList(Wrapper<P> wrapper, Class<D> clazz) {
        // 获取原始 Mapper 接口 Class（解决代理问题）
        Class<P> entityClazz = (Class<P>) TypeUtil.getTypeArgument(this.getClass(), BaseMapper.class);

        Class<? extends BaseMapper<P>> mapperInterface = (Class<? extends BaseMapper<P>>) Ztream.of(this.getClass().getInterfaces()).filter(BaseMapper.class::isAssignableFrom).getFirst();
        SqlSession sqlSession = SqlHelper.sqlSession(entityClazz);
        // 从缓存获取或构建 MappedStatement
        String msId = ExecuteHelper.buildDynamicMappedStatement(sqlSession.getConfiguration(), SqlMethod.SELECT_LIST, clazz, mapperInterface);

        return sqlSession.selectList(msId, Collections.singletonMap(Constants.WRAPPER, wrapper));
    }

    default <D> List<D> selectList(Consumer<LambdaQueryExWrapper<P>> consumer, Class<D> clazz) {
        return ExecuteHelper.execute(this, consumer, (m, w) -> m.selectList(w, clazz), LambdaQueryExWrapper::new);
    }
}
