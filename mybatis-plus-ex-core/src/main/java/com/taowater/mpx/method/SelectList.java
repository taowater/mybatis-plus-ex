package com.taowater.mpx.method;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

/**
 * 查询满足条件所有数据
 * 支持limit
 * <p>
 * 方言由 {@link ExMethodSqlInjector} 注入，本类不再自行探测 DbType。
 *
 * @author zhu56
 */
@SuppressWarnings("unused")
class SelectList extends AbstractMethod {

    private final DbType dbType;

    public SelectList() {
        this(DbType.OTHER);
    }

    public SelectList(DbType dbType) {
        super(SqlExMethod.SELECT_LIST.getMethod());
        this.dbType = dbType == null ? DbType.OTHER : dbType;
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        String sql = buildLimitSql(tableInfo);
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        return this.addSelectMappedStatementForTable(mapperClass, methodName, sqlSource, tableInfo);
    }

    private String buildLimitSql(TableInfo tableInfo) {
        String sqlFirst = sqlFirst();
        String selectColumns = sqlSelectColumns(tableInfo, true);
        String tableName = tableInfo.getTableName();
        String whereClause = sqlWhereEntityWrapper(true, tableInfo);
        String sqlOrderBy = sqlOrderBy(tableInfo);
        String sqlLimit = sqlLimit(dbType);
        String sqlComment = sqlComment();
        switch (dbType) {
            case ORACLE:
            case ORACLE_12C:
                // ROWNUM 限制仅在传入 limit 时追加，避免无 limit 时产生非法/意外 SQL
                String rownum = SqlScriptUtils.convertIf(" WHERE ROWNUM <= ${ew.limit}", limitCondition(), true);
                return String.format("<script>SELECT * FROM ( %s SELECT %s FROM %s %s %s %s\n)%s\n</script>", sqlFirst,
                        selectColumns, tableName, whereClause, sqlOrderBy, sqlComment, rownum);
            case SQL_SERVER:
                return String.format("<script>%s SELECT %s %s FROM %s %s %s %s\n</script>",
                        sqlFirst, sqlLimit, selectColumns, tableName, whereClause, sqlOrderBy, sqlComment);
            default:
                return String.format(SqlExMethod.SELECT_LIST.getSql(),
                        sqlFirst, selectColumns, tableInfo.getTableName(), whereClause, sqlOrderBy, sqlLimit, sqlComment);
        }
    }

    /**
     * limit 生效条件：ew 为扩展查询 wrapper 且其 limit 非空。
     */
    private String limitCondition() {
        return String.format("%s != null and (ew instanceof com.taowater.mpx.wrapper.QueryExWrapper or ew instanceof com.taowater.mpx.wrapper.LambdaQueryExWrapper) and %s != null", WRAPPER, "ew.limit");
    }

    protected String sqlLimit(DbType dbType) {
        String limitFormat = "LIMIT ${ew.limit}";
        if (dbType == DbType.SQL_SERVER) {
            limitFormat = "TOP ${ew.limit}";
        } else if (dbType == DbType.DB2) {
            limitFormat = "FETCH FIRST ${ew.limit} ROWS ONLY";
        }
        return NEWLINE + SqlScriptUtils.convertIf(limitFormat, limitCondition(), true);
    }
}
