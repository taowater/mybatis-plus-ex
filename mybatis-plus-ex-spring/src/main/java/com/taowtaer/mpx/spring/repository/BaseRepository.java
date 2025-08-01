package com.taowtaer.mpx.spring.repository;

import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import com.taowater.mpx.mapper.BaseMapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 基础持久层操作
 *
 * @author zhu56
 * @see CrudRepository
 */
@SuppressWarnings("unused")
public abstract class BaseRepository<M extends BaseMapper<T>, T> extends DynamicRepository<T> {

    @Autowired
    protected M baseMapper;

    @Override
    public M getBaseMapper() {
        return baseMapper;
    }
}