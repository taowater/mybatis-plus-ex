package com.taowater.mpx.fill;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;

/** Identifies the injected update and retains its actual entity metadata. */
public final class WrapperFillSqlSource implements SqlSource {

    private final SqlSource delegate;
    private final TableInfo tableInfo;

    public WrapperFillSqlSource(SqlSource delegate, TableInfo tableInfo) {
        this.delegate = delegate;
        this.tableInfo = tableInfo;
    }

    public TableInfo getTableInfo() {
        return tableInfo;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return delegate.getBoundSql(parameterObject);
    }
}
