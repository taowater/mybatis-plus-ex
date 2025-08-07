package com.taowater.mpx.method;

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
import java.util.Objects;

/**
 * 查询满足条件所有数据
 * 支持limit
 *
 * @author zhu56
 */
@SuppressWarnings("unused")
class SelectList extends AbstractMethod {

    private DbType dbType;

    public SelectList() {
        super(SqlExMethod.SELECT_LIST.getMethod());
    }

    public SelectList(DbType dbType) {
        super(SqlExMethod.SELECT_LIST.getMethod());
        this.dbType = dbType;
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        DbType dbType = getDbType();
        String sql = buildLimitSql(tableInfo);
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        return this.addSelectMappedStatementForTable(mapperClass, methodName, sqlSource, tableInfo);
    }

    private String buildLimitSql(TableInfo tableInfo) {
        String sqlFirst = sqlFirst();
        DbType dbType = getDbType();
        String selectColumns = sqlSelectColumns(tableInfo, true);
        String tableName = tableInfo.getTableName();
        String whereClause = sqlWhereEntityWrapper(true, tableInfo);
        String sqlOrderBy = sqlOrderBy(tableInfo);
        String sqlLimit = sqlLimit(dbType);
        String sqlComment = sqlComment();
        switch (dbType) {
            case ORACLE:
            case ORACLE_12C:
                return String.format("<script>SELECT * FROM ( %s SELECT %s FROM %s %s %s %s\n) WHERE ROWNUM <= ${limit}\n</script>", sqlFirst,
                        selectColumns, tableName, whereClause, sqlOrderBy, sqlComment);
            case SQL_SERVER:
                return String.format("<script>%s SELECT %s %s FROM %s %s %s %s\n</script>",
                        sqlFirst, sqlLimit, selectColumns, tableName, whereClause, sqlOrderBy, sqlComment);
            default:
                return String.format(SqlExMethod.SELECT_LIST.getSql(),
                        sqlFirst, selectColumns, tableInfo.getTableName(), whereClause, sqlOrderBy, sqlLimit, sqlComment);
        }
    }

    protected String sqlLimit(DbType dbType) {
        String limitFormat = "LIMIT ${ew.limit}";
        if (dbType.equals(DbType.SQL_SERVER)) {
            limitFormat = "TOP ${ew.limit}";
        } else if (dbType.equals(DbType.DB2)) {
            limitFormat = "FETCH FIRST ${ew.limit} ROWS ONLY";
        }
        return NEWLINE + SqlScriptUtils.convertIf(limitFormat, String.format("%s != null and (ew instanceof com.taowater.mpx.wrapper.QueryExWrapper or ew instanceof com.taowater.mpx.wrapper.LambdaQueryExWrapper) and %s != null", WRAPPER, "ew.limit"), true);
    }

    /**
     * 获取当前数据库类型（正确方式）
     */
    private DbType getDbType() {
        if (Objects.nonNull(this.dbType)) {
            return this.dbType;
        }
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