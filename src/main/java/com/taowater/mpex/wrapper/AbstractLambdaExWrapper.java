package com.taowater.mpex.wrapper;

import com.baomidou.mybatisplus.core.conditions.AbstractLambdaWrapper;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.taowater.mpex.wrapper.interfaces.CompareEx;
import lombok.Getter;
import lombok.Setter;

/**
 * 抽象LambdaExWrapper
 * 由于直接继承LambdaWrapper会有断链的问题，所以选择直接抄一遍LambdaWrapper作为新的中间层
 *
 * @author zhu56
 */
@Setter
@Getter
public abstract class AbstractLambdaExWrapper<T, W extends AbstractLambdaWrapper<T, W>>
        extends AbstractLambdaWrapper<T, W>
        implements CompareEx<W, SFunction<T, ?>> {

    /**
     * 是否需要查询
     */
    private boolean execute;

    @Override
    public boolean needExecute() {
        return execute;
    }

    @Override
    public W addConditionCol(boolean condition, SFunction<T, ?> column1, SqlKeyword sqlKeyword, SFunction<T, ?> column2) {
        return maybeDo(condition, () -> appendSqlSegments(columnToSqlSegment(column1), sqlKeyword,
                columnToSqlSegment(column2)));
    }
}
