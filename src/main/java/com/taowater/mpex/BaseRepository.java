package com.taowater.mpex;

import com.baomidou.mybatisplus.extension.repository.CrudRepository;

/**
 * 基础持久层操作
 *
 * @author zhu56
 */
@SuppressWarnings("unused")
public abstract class BaseRepository<M extends BaseMapper<T>, T> extends CrudRepository<M, T> implements IBaseRepository<T> {
}