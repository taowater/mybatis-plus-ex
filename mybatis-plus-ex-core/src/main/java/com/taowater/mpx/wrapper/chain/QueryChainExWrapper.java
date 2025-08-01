package com.taowater.mpx.wrapper.chain;

import com.baomidou.mybatisplus.core.enums.SqlKeyword;
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

    private final BaseMapper<T> baseMapper;

    public QueryChainExWrapper(BaseMapper<T> baseMapper) {
        super();
        this.baseMapper = baseMapper;
        super.wrapperChildren = new QueryExWrapper<>();
    }

    public QueryChainExWrapper(Class<T> entityClass) {
        super();
        this.baseMapper = null;
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

    @Override
    public BaseMapper<T> getBaseMapper() {
        return baseMapper;
    }

    public LambdaQueryChainExWrapper<T> lambda() {
        return new LambdaQueryChainExWrapper<>(
                baseMapper,
                wrapperChildren.lambda()
        );
    }

    @Override
    public QueryChainExWrapper<T> addConditionCol(boolean condition, String column1, SqlKeyword sqlKeyword, String column2) {
        wrapperChildren.addConditionCol(condition, column1, sqlKeyword, column2);
        return typedThis;
    }
}
