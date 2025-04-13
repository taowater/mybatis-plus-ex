package com.taowater.mpex.wrapper;

import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.Update;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.taowater.mpex.wrapper.interfaces.UpdateSelf;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 拓展Lambda更新wrapper
 *
 * @author zhu56
 * @date 2023/09/02 00:46
 * @see LambdaUpdateWrapper
 */
public class LambdaUpdateExWrapper<T> extends AbstractLambdaExWrapper<T, LambdaUpdateExWrapper<T>>
        implements Update<LambdaUpdateExWrapper<T>, SFunction<T, ?>>, UpdateSelf<LambdaUpdateExWrapper<T>, SFunction<T, ?>> {

    private final List<String> sqlSet;

    public LambdaUpdateExWrapper() {
        // 如果无参构造函数，请注意实体 NULL 情况 SET 必须有否则 SQL 异常
        this((T) null);
    }

    public LambdaUpdateExWrapper(T entity) {
        super.setEntity(entity);
        super.initNeed();
        this.sqlSet = new ArrayList<>();
    }

    public LambdaUpdateExWrapper(Class<T> entityClass) {
        super.setEntityClass(entityClass);
        super.initNeed();
        this.sqlSet = new ArrayList<>();
    }

    LambdaUpdateExWrapper(T entity, Class<T> entityClass, List<String> sqlSet, AtomicInteger paramNameSeq,
                          Map<String, Object> paramNameValuePairs, MergeSegments mergeSegments, SharedString paramAlias,
                          SharedString lastSql, SharedString sqlComment, SharedString sqlFirst) {
        super.setEntity(entity);
        super.setEntityClass(entityClass);
        this.sqlSet = sqlSet;
        this.paramNameSeq = paramNameSeq;
        this.paramNameValuePairs = paramNameValuePairs;
        this.expression = mergeSegments;
        this.paramAlias = paramAlias;
        this.lastSql = lastSql;
        this.sqlComment = sqlComment;
        this.sqlFirst = sqlFirst;
    }

    @Override
    public LambdaUpdateExWrapper<T> set(boolean condition, SFunction<T, ?> column, Object val, String mapping) {
        return maybeDo(condition, () -> {
            String sql = formatParam(mapping, val);
            sqlSet.add(columnToString(column) + Constants.EQUALS + sql);
        });
    }

    @Override
    public LambdaUpdateExWrapper<T> setSql(boolean condition, String setSql, Object... params) {
        if (condition && StringUtils.isNotBlank(setSql)) {
            sqlSet.add(formatSqlMaybeWithParam(setSql, params));
        }
        return typedThis;
    }

    @Override
    public LambdaUpdateExWrapper<T> setIncrBy(boolean condition, SFunction<T, ?> column, Number val) {
        return maybeDo(condition, () -> {
            String realColumn = columnToString(column);
            sqlSet.add(String.format("%s=%s + %s", realColumn, realColumn, val instanceof BigDecimal ? ((BigDecimal) val).toPlainString() : val));
        });
    }

    @Override
    public LambdaUpdateExWrapper<T> setDecrBy(boolean condition, SFunction<T, ?> column, Number val) {
        return maybeDo(condition, () -> {
            String realColumn = columnToString(column);
            sqlSet.add(String.format("%s=%s - %s", realColumn, realColumn, val instanceof BigDecimal ? ((BigDecimal) val).toPlainString() : val));
        });
    }

    @Override
    public String getSqlSet() {
        if (CollectionUtils.isEmpty(sqlSet)) {
            return null;
        }
        return String.join(Constants.COMMA, sqlSet);
    }

    @Override
    protected LambdaUpdateExWrapper<T> instance() {
        return new LambdaUpdateExWrapper<>(getEntity(), getEntityClass(), null, paramNameSeq, paramNameValuePairs,
                new MergeSegments(), paramAlias, SharedString.emptyString(), SharedString.emptyString(), SharedString.emptyString());
    }

    @Override
    public void clear() {
        super.clear();
        sqlSet.clear();
    }

    /**
     * 置空
     *
     * @param column 字段
     * @return {@link LambdaUpdateExWrapper}<{@link T}>
     */
    public LambdaUpdateExWrapper<T> setNull(SFunction<T, ?> column) {
        return this.set(column, null);
    }

    /**
     * 字段自操作
     *
     * @param condition 条件
     * @param column    字段
     * @param keyword   关键字
     * @param val       值
     * @return {@link LambdaUpdateExWrapper}<{@link T}>
     */
    protected LambdaUpdateExWrapper<T> self(boolean condition, SFunction<T, ?> column, String keyword, Object val) {
        return maybeDo(condition, () -> sqlSet.add(MessageFormat.format("{0}={0}{1}{2}", columnToString(column), keyword, formatParam(null, val))));
    }

    @Override
    public <N extends Number> LambdaUpdateExWrapper<T> incr(boolean condition, SFunction<T, ?> column, N increment) {
        return self(condition, column, Constants.PLUS, increment);
    }

    @Override
    public <N extends Number> LambdaUpdateExWrapper<T> decr(boolean condition, SFunction<T, ?> column, N decrement) {
        return self(condition, column, Constants.DASH, decrement);
    }
}
