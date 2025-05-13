package com.taowtaer.mpx.spring.repository;

import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import com.taowater.mpx.mapper.BaseMapper;

/**
 * 基础持久层操作
 *
 * @author zhu56
 */
@SuppressWarnings("unused")
public abstract class BaseRepository<M extends BaseMapper<T>, T> extends CrudRepository<M, T> implements IBaseRepository<T> {
}