package com.taowater.mpx.method;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import com.taowater.mpx.fill.WrapperFillContext;
import com.taowater.mpx.fill.WrapperFillSqlSource;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

/** Keeps MP's entity/Wrapper update SQL and adds an optional, separately bound defaults SET. */
class UpdateWithWrapperFill extends com.baomidou.mybatisplus.core.injector.methods.Update {

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        String sql = SqlMethod.UPDATE.format(tableInfo.getTableName(),
                sqlSet(true, true, tableInfo, true, ENTITY, ENTITY_DOT),
                sqlWhereEntityWrapper(true, tableInfo), sqlComment());
        SqlSource source = new WrapperFillSqlSource(createSqlSource(configuration, sql, modelClass), tableInfo);
        return addUpdateMappedStatement(mapperClass, modelClass, methodName, source);
    }

    @Override
    protected String sqlSet(boolean logic, boolean ew, TableInfo table, boolean judgeAliasNull,
                            String alias, String prefix) {
        String script = table.getAllSqlSet(logic, prefix);
        if (judgeAliasNull) {
            script = SqlScriptUtils.convertIf(script, alias + " != null", true);
        }
        if (ew) {
            script += NEWLINE + SqlScriptUtils.convertIf(
                    SqlScriptUtils.unSafeParam(U_WRAPPER_SQL_SET) + ",",
                    WRAPPER + " != null and " + U_WRAPPER_SQL_SET + " != null", true);
        }
        String key = WrapperFillContext.PARAMETER;
        script += NEWLINE + SqlScriptUtils.convertIf("${" + key + ".sqlSet}",
                "_parameter.containsKey('" + key + "')", true);
        return SqlScriptUtils.convertSet(script);
    }
}
