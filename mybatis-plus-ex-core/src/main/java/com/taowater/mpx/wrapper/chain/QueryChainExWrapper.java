package com.taowater.mpx.wrapper.chain;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.taowater.mpx.wrapper.QueryExWrapper;
import com.taowater.mpx.wrapper.interfaces.ChainQueryEx;
import com.taowater.mpx.wrapper.interfaces.QueryEx;

import java.util.List;
import java.util.function.Predicate;

/**
 * 拓展查询链式wrapper
 *
 * @see QueryChainWrapper
 */
public class QueryChainExWrapper<T> extends AbstractChainExWrapper<T, String, QueryChainExWrapper<T>, QueryExWrapper<T>>
        implements ChainQueryEx<T>, QueryEx<QueryChainExWrapper<T>, T, String> {

    public QueryChainExWrapper(BaseMapper<T> baseMapper) {
        super(baseMapper);
        super.wrapperChildren = new QueryExWrapper<>();
    }

    public QueryChainExWrapper(Class<T> entityClass) {
        super(null);
        super.wrapperChildren = new QueryExWrapper<>(entityClass);

    }

    @Override
    public void setLimit(Integer limit) {
        wrapperChildren.setLimit(limit);
    }

    @Override
    public QueryChainExWrapper<T> select(boolean condition, List<String> columns) {
        wrapperChildren.select(condition, columns);
        return typedThis;
    }

    @Override
    public QueryChainExWrapper<T> select(Class<T> entityClass, Predicate<TableFieldInfo> predicate) {
        wrapperChildren.select(entityClass, predicate);
        return typedThis;
    }

    @Override
    public String getSqlSelect() {
        throw ExceptionUtils.mpe("can not use this method for \"%s\"", "getSqlSelect");
    }

    public LambdaQueryChainExWrapper<T> lambda() {
        return new LambdaQueryChainExWrapper<>(
                getBaseMapper(),
                wrapperChildren.lambda()
        );
    }
}
