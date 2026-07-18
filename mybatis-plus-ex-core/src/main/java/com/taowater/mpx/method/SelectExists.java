package com.taowater.mpx.method;

import com.baomidou.mybatisplus.annotation.DbType;
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

    private final DbType dbType;

    public SelectExists() {
        this(null);
    }

    public SelectExists(DbType dbType) {
        super(SqlExMethod.SELECT_EXISTS.getMethod());
        this.dbType = dbType;
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        String sql = buildExistsSql(tableInfo);
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        return addSelectMappedStatementForOther(mapperClass, methodName, sqlSource, Integer.class);
    }

    private String buildExistsSql(TableInfo tableInfo) {
        String tableName = tableInfo.getTableName();
        String where = sqlWhereEntityWrapper(true, tableInfo);
        String template = existsSqlTemplate(dbType);
        return String.format(template, tableName, where);
    }

    /**
     * Oracle 需要 FROM DUAL；SQL Server 用 CASE WHEN EXISTS；其余方言可用无 FROM 的 SELECT 1 WHERE EXISTS。
     */
    private static String existsSqlTemplate(DbType dbType) {
        if (dbType == null) {
            return SqlExMethod.SELECT_EXISTS.getSql();
        }
        switch (dbType) {
            case ORACLE:
            case ORACLE_12C:
                return "<script>SELECT 1 FROM DUAL WHERE EXISTS (SELECT 1 FROM %s %s)\n</script>";
            case SQL_SERVER:
            case SQL_SERVER2005:
                return "<script>SELECT CASE WHEN EXISTS (SELECT 1 FROM %s %s) THEN 1 END\n</script>";
            default:
                return SqlExMethod.SELECT_EXISTS.getSql();
        }
    }
}
