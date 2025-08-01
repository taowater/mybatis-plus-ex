package com.taowater.mpx.wrapper;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlInjectionUtils;
import com.taowater.mpx.wrapper.interfaces.CompareEx;
import com.taowater.mpx.wrapper.interfaces.UpdateEx;
import lombok.Setter;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 拓展更新wrapper
 *
 * @author zhu56
 * @see UpdateWrapper
 */
public class UpdateExWrapper<T> extends AbstractWrapper<T, String, UpdateExWrapper<T>>
        implements CompareEx<UpdateExWrapper<T>, String>, UpdateEx<UpdateExWrapper<T>, String> {
    /**
     * 是否需要查询
     */
    @Setter
    private boolean execute = true;

    @Override
    public boolean needExecute() {
        return execute;
    }

    /**
     * SQL 更新字段内容，例如：name='1', age=2
     */
    private final List<String> sqlSet;

    public UpdateExWrapper() {
        // 如果无参构造函数，请注意实体 NULL 情况 SET 必须有否则 SQL 异常
        this(null);
    }

    public UpdateExWrapper(T entity) {
        super.setEntity(entity);
        super.initNeed();
        this.sqlSet = new ArrayList<>();
    }

    private UpdateExWrapper(T entity, List<String> sqlSet, AtomicInteger paramNameSeq,
                            Map<String, Object> paramNameValuePairs, MergeSegments mergeSegments, SharedString paramAlias,
                            SharedString lastSql, SharedString sqlComment, SharedString sqlFirst) {
        super.setEntity(entity);
        this.sqlSet = sqlSet;
        this.paramNameSeq = paramNameSeq;
        this.paramNameValuePairs = paramNameValuePairs;
        this.expression = mergeSegments;
        this.paramAlias = paramAlias;
        this.lastSql = lastSql;
        this.sqlComment = sqlComment;
        this.sqlFirst = sqlFirst;
    }


    /**
     * 检查 SQL 注入过滤
     */
    private boolean checkSqlInjection;

    /**
     * 开启检查 SQL 注入
     */
    public UpdateExWrapper<T> checkSqlInjection() {
        this.checkSqlInjection = true;
        return this;
    }

    @Override
    protected String columnToString(String column) {
        if (checkSqlInjection && SqlInjectionUtils.check(column)) {
            throw new MybatisPlusException("Discovering SQL injection column: " + column);
        }
        return column;
    }

    @Override
    public String getSqlSet() {
        if (CollectionUtils.isEmpty(sqlSet)) {
            return null;
        }
        return String.join(Constants.COMMA, sqlSet);
    }

    @Override
    public UpdateExWrapper<T> set(boolean condition, String column, Object val, String mapping) {
        return maybeDo(condition, () -> {
            String sql = formatParam(mapping, val);
            sqlSet.add(column + Constants.EQUALS + sql);
        });
    }

    @Override
    public UpdateExWrapper<T> setSql(boolean condition, String setSql, Object... params) {
        return maybeDo(condition && StringUtils.isNotBlank(setSql), () -> {
            sqlSet.add(formatSqlMaybeWithParam(setSql, params));
        });
    }


    /**
     * 返回一个支持 lambda 函数写法的 wrapper
     */
    public LambdaUpdateExWrapper<T> lambda() {
        return new LambdaUpdateExWrapper<>(getEntity(), getEntityClass(), sqlSet, paramNameSeq, paramNameValuePairs,
                expression, paramAlias, lastSql, sqlComment, sqlFirst);
    }

    @Override
    protected UpdateExWrapper<T> instance() {
        return new UpdateExWrapper<>(getEntity(), null, paramNameSeq, paramNameValuePairs, new MergeSegments(),
                paramAlias, SharedString.emptyString(), SharedString.emptyString(), SharedString.emptyString());
    }

    @Override
    public void clear() {
        super.clear();
        sqlSet.clear();
    }

    /**
     * 字段自操作
     *
     * @param condition 条件
     * @param column    字段
     * @param keyword   关键字
     * @param val       值
     */
    @Override
    public UpdateExWrapper<T> self(boolean condition, String column, String keyword, Object val) {
        return maybeDo(condition, () -> sqlSet.add(MessageFormat.format("{0}={0}{1}{2}", columnToString(column), keyword, val instanceof BigDecimal ? ((BigDecimal) val).toPlainString() : val)));
    }


    public final UpdateExWrapper<T> addConditionCol(boolean condition, String column1, SqlKeyword sqlKeyword, String column2) {
        return maybeDo(condition, () -> appendSqlSegments(columnToSqlSegment(column1), sqlKeyword,
                columnToSqlSegment(column2)));
    }
}
