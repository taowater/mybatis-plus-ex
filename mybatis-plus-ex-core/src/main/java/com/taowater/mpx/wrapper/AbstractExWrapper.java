package com.taowater.mpx.wrapper;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlInjectionUtils;
import com.taowater.mpx.wrapper.interfaces.CompareEx;
import lombok.Getter;
import lombok.Setter;

/**
 * 查询条件封装拓展
 *
 * @see AbstractWrapper
 */
public abstract class AbstractExWrapper<T, R, Children extends AbstractExWrapper<T, R, Children>> extends AbstractWrapper<T, R, Children> implements CompareEx<Children, R> {
    /**
     * 是否需要查询
     */
    @Setter
    private boolean execute = true;

    /**
     * 检查 SQL 注入过滤
     */
    @Getter
    private boolean checkSqlInjection;

    /**
     * 开启检查 SQL 注入
     */
    public Children checkSqlInjection() {
        this.checkSqlInjection = true;
        return typedThis;
    }

    @Override
    public boolean needExecute() {
        return execute;
    }

    @Override
    public Children addConditionCol(boolean condition, R column1, SqlKeyword sqlKeyword, R column2) {
        return maybeDo(condition, () -> appendSqlSegments(columnToSqlSegment(column1), sqlKeyword,
                columnToSqlSegment(column2)));
    }

    @Override
    protected String columnToString(R column) {
        if (column instanceof String) {
            if (isCheckSqlInjection() && SqlInjectionUtils.check((String) column)) {
                throw new MybatisPlusException("Discovering SQL injection column: " + column);
            }
            return (String) column;
        }
        return super.columnToString(column);
    }
}
