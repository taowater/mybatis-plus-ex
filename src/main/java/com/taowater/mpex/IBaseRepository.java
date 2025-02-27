package com.taowater.mpex;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.repository.IRepository;
import com.taowater.taol.core.util.EmptyUtil;
import com.taowater.ztream.Any;
import com.taowater.ztream.Ztream;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 快速查询
 *
 * @author 朱滔
 * @date 2022/11/20 02:23:15
 */
public interface IBaseRepository<M extends BaseMapper<P>, P> extends IRepository<P> {

    @Override
    M getBaseMapper();

    /**
     * 单属性等值匹配查询列表
     *
     * @param field 属性
     * @param value 值
     * @return {@link List}<{@link P}>
     */
    default <V extends Serializable> List<P> list(SFunction<P, V> field, V value) {
        if (EmptyUtil.isHadEmpty(field, value)) {
            return new ArrayList<>(0);
        }
        return this.list(w -> w.eq(field, value));
    }

    /**
     * 单属性in匹配查询列表
     *
     * @param field 属性
     * @param c     集合
     * @return {@link List}<{@link P}>
     */
    default <V extends Serializable> List<P> list(SFunction<P, V> field, Collection<V> c) {
        if (EmptyUtil.isHadEmpty(field, c)) {
            return new ArrayList<>(0);
        }
        return this.list(w -> w.in(field, c));
    }

    /**
     * 单属性等值匹配查询单个
     *
     * @param field 属性
     * @param value 值
     * @return {@link P}
     */
    default <V extends Serializable> P getOne(SFunction<P, V> field, V value) {
        if (EmptyUtil.isHadEmpty(field, value)) {
            return null;
        }
        return this.getOne(w -> w.eq(field, value));
    }

    /**
     * 单属性等值匹配删除
     *
     * @param field 属性
     * @param value 值
     * @return boolean
     */
    default <V extends Serializable> boolean remove(SFunction<P, V> field, V value) {
        if (EmptyUtil.isHadEmpty(field, value)) {
            return false;
        }
        return this.remove(w -> w.eq(field, value));
    }

    /**
     * 单属性in匹配删除
     *
     * @param field 属性
     * @param c     集合
     * @return boolean
     */
    default <V extends Serializable> boolean remove(SFunction<P, V> field, Collection<V> c) {
        if (EmptyUtil.isHadEmpty(field, c)) {
            return false;
        }
        return this.remove(w -> w.in(field, c));
    }

    /**
     * 查询列表
     *
     * @return {@link List}<{@link P}>
     */
    default List<P> list() {
        return getBaseMapper().selectList();
    }

    /**
     * 查询列表
     *
     * @param consumer 处理流程
     * @return {@link List}<{@link P}>
     */
    default List<P> list(Consumer<LambdaQueryExWrapper<P>> consumer) {
        return getBaseMapper().selectList(consumer);
    }

    /**
     * 分页查询
     *
     * @param page     分页参数
     * @param consumer 处理流程
     * @return {@link E}
     */
    default <E extends IPage<P>> E page(E page, Consumer<LambdaQueryExWrapper<P>> consumer) {
        return getBaseMapper().selectPage(page, consumer);
    }

    /**
     * 获取一个
     *
     * @param consumer 处理流程
     * @return {@link P}
     */
    default P getOne(Consumer<LambdaQueryExWrapper<P>> consumer) {
        return getBaseMapper().selectOne(consumer);
    }

    /**
     * 获取一个，结果用Any包装
     *
     * @param consumer 处理流程
     * @return {@link Any}<{@link P}>
     */
    default Any<P> getOneX(Consumer<LambdaQueryExWrapper<P>> consumer) {
        return getBaseMapper().selectOneX(consumer);
    }

    /**
     * 获取一个
     *
     * @param consumer 处理流程
     * @param throwEx  多个结果是否抛出异常
     * @return {@link P}
     */
    default P getOne(Consumer<LambdaQueryExWrapper<P>> consumer, boolean throwEx) {
        return getBaseMapper().selectOne(consumer, throwEx);
    }

    /**
     * 删除
     *
     * @param consumer 操作符
     * @return boolean
     */
    default boolean remove(Consumer<LambdaQueryExWrapper<P>> consumer) {
        return getBaseMapper().delete(consumer) > 0;
    }

    /**
     * 更新
     *
     * @param consumer 操作符
     * @return boolean
     */
    default boolean update(Consumer<LambdaUpdateExWrapper<P>> consumer) {
        return getBaseMapper().update(consumer) > 0;
    }

    /**
     * 查询前n个
     *
     * @param consumer 操作
     * @param limit    限制
     * @return {@link List}<{@link P}>
     */
    default List<P> limit(Consumer<LambdaQueryExWrapper<P>> consumer, int limit) {
        return getBaseMapper().selectLimit(consumer, limit);
    }

    /**
     * 统计
     *
     * @return long
     */
    default long count() {
        return getBaseMapper().selectCount();
    }

    /**
     * 统计
     *
     * @param consumer 操作
     * @return long
     */
    default long count(Consumer<LambdaQueryExWrapper<P>> consumer) {
        return getBaseMapper().selectCount(consumer);
    }

    /**
     * 查询结果为流
     *
     * @return {@link Ztream}<{@link P}>
     */
    default Ztream<P> ztream() {
        return getBaseMapper().selectZtream();
    }

    /**
     * 查询结果为流
     *
     * @param consumer 处理流程
     * @return {@link Ztream}<{@link P}>
     */
    default Ztream<P> ztream(Consumer<LambdaQueryExWrapper<P>> consumer) {
        return getBaseMapper().selectZtream(consumer);
    }

    /**
     * 指定属性查询并按该属性分组
     *
     * @param field  属性
     * @param values 值集合
     * @return {@link Map}<{@link K}, {@link List}<{@link P}>>
     */
    default <K extends Serializable> Map<K, List<P>> group(SFunction<P, K> field, Collection<K> values) {
        return getBaseMapper().groupBy(field, values);
    }

    /**
     * 指定属性查询并按该属性分组,按指定方法处理得到分组值
     *
     * @param field  属性
     * @param values 值集合
     * @param funV   分组值处理方法
     * @return {@link Map}<{@link K}, {@link List}<{@link A}>>
     */
    default <K extends Serializable, A> Map<K, List<A>> group(SFunction<P, K> field, Collection<K> values, Function<P, A> funV) {
        return getBaseMapper().groupBy(field, values, funV);
    }

    /**
     * 指定属性查询并按该属性分组,按转换类型得到分组值
     *
     * @param field  属性
     * @param values 值集合
     * @param clazzV 分组值转换类型
     * @return {@link Map}<{@link K}, {@link List}<{@link A}>>
     */
    default <K extends Serializable, A> Map<K, List<A>> group(SFunction<P, K> field, Collection<K> values, Class<A> clazzV) {
        return getBaseMapper().groupBy(field, values, clazzV);
    }


    /**
     * 指定属性查询得到[该属性为key，对应数据为value的map]
     *
     * @param field  属性
     * @param values 值集合
     * @return {@link Map}<{@link K}, {@link P}>
     */
    default <K extends Serializable> Map<K, P> map(SFunction<P, K> field, Collection<K> values) {
        return getBaseMapper().mapBy(field, values);
    }


    /**
     * 指定属性查询得到[该属性为key，指定方法处理后的值为value的map]
     *
     * @param field  属性
     * @param values 值集合
     * @param funV   值处理方法
     * @return {@link Map}<{@link K}, {@link A}>
     */
    default <K extends Serializable, A> Map<K, A> map(SFunction<P, K> field, Collection<K> values, Function<P, A> funV) {
        return getBaseMapper().mapBy(field, values, funV);
    }

    /**
     * 指定属性查询得到[该属性为key，指定方法处理后的值为value的map]
     *
     * @param field  属性
     * @param values 值集合
     * @param clazzV 值转换类型
     * @return {@link Map}<{@link K}, {@link A}>
     */
    default <K extends Serializable, A> Map<K, A> map(SFunction<P, K> field, Collection<K> values, Class<A> clazzV) {
        return getBaseMapper().mapBy(field, values, clazzV);
    }
}
