package com.taowater.mpex.method;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 限制条数选择
 *
 * @author zhu56
 * @date 2025/04/10 01:59
 */
class SelectLimit extends AbstractMethod {

    public SelectLimit() {
        super(SqlExMethod.SELECT_LIMIT.getMethod());
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        DbType dbType = getDbType();
        String sql = buildLimitSql(tableInfo, dbType);
        SqlSource sqlSource = super.createSqlSource(configuration, sql, modelClass);
        return this.addSelectMappedStatementForTable(mapperClass, methodName, sqlSource, tableInfo);
    }

    private String buildLimitSql(TableInfo tableInfo, DbType dbType) {
        String sqlFirst = sqlFirst();
        String selectColumns = sqlSelectColumns(tableInfo, true);
        String tableName = tableInfo.getTableName();
        String whereClause = sqlWhereEntityWrapper(true, tableInfo);
        String sqlOrderBy = sqlOrderBy(tableInfo);
        String sqlLimit = sqlLimit(dbType);
        String sqlComment = sqlComment();
        switch (dbType) {
            // Oracle 使用 ROWNUM
            case ORACLE:
            case ORACLE_12C:
                return String.format("<script>SELECT * FROM ( %s SELECT %s FROM %s %s %s %s\n) WHERE ROWNUM <= ${limit}\n</script>", sqlFirst,
                        selectColumns, tableName, whereClause, sqlOrderBy, sqlComment);
            // SQL Server 使用 TOP
            case SQL_SERVER:
                return String.format("<script>%s SELECT %s %s FROM %s %s %s %s\n</script>",
                        sqlFirst, sqlLimit, selectColumns, tableName, whereClause, sqlOrderBy, sqlComment);
            default:
                return String.format(SqlExMethod.SELECT_LIMIT.getSql(),
                        sqlFirst, selectColumns, tableInfo.getTableName(), whereClause, sqlOrderBy, sqlLimit, sqlComment);
        }
    }

    protected String sqlLimit(DbType dbType) {
        String limitFormat = "LIMIT ${limit}";
        if (dbType.equals(DbType.SQL_SERVER)) {
            limitFormat = "TOP ${limit}";
        } else if (dbType.equals(DbType.DB2)) {
            limitFormat = "FETCH FIRST ${limit} ROWS ONLY";
        }
        return NEWLINE + SqlScriptUtils.convertIf(limitFormat, "limit != null", true);
    }

    /**
     * 获取当前数据库类型（正确方式）
     */
    private DbType getDbType() {
        Connection connection = null;
        try {
            DataSource dataSource = configuration.getEnvironment().getDataSource();
            connection = dataSource.getConnection();
            return JdbcUtils.getDbType(connection.getMetaData().getURL());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to determine database type", e);
        } finally {
            // 手动关闭连接（确保绝对释放）
            if (connection != null) {
                try {
                    if (!connection.isClosed()) {
                        connection.close();
                    }
                } catch (SQLException ignored) {
                }
            }
        }

    }
}