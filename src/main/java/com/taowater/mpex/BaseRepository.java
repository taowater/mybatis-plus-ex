package com.taowater.mpex;

import com.baomidou.mybatisplus.extension.repository.CrudRepository;

/**
 * @author zhu56
 */
@SuppressWarnings("unused")
public abstract class BaseRepository<M extends BaseMapper<T>, T> extends CrudRepository<M, T> implements IBaseRepository<M, T> {
}