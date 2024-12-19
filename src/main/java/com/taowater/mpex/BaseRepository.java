package com.taowater.mpex;

import com.baomidou.mybatisplus.extension.repository.CrudRepository;

public abstract class BaseRepository<M extends BaseMapper<T>, T> extends CrudRepository<M, T> implements IBaseRepository<M, T> {
}