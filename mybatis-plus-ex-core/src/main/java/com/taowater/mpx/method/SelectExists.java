package com.taowater.mpx.method;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

/**
 * 判断条件数据是否存在
 *
 * @author zhu56
 */
class SelectExists extends AbstractMethod {

    public SelectExists() {
        super(SqlExMethod.SELECT_EXISTS.getMethod());
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        SqlExMethod sqlMethod = SqlExMethod.SELECT_EXISTS;
        String formattedSql = String.format(sqlMethod.getSql(), tableInfo.getTableName(), sqlWhereEntityWrapper(true, tableInfo));
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, formattedSql, modelClass);
        return addSelectMappedStatementForOther(mapperClass, methodName, sqlSource, Integer.class);
    }
}