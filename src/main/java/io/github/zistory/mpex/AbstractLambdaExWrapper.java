package io.github.zistory.mpex;

import com.baomidou.mybatisplus.core.conditions.AbstractLambdaWrapper;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.Getter;
import lombok.Setter;

/**
 * 抽象LambdaExWrapper
 * 由于直接继承LambdaWrapper会有断链的问题，所以选择直接抄一遍LambdaWrapper作为新的中间层
 *
 * @author zhu56
 * @date 2023/09/02 01:11
 */
@Getter
public abstract class AbstractLambdaExWrapper<T, W extends AbstractLambdaWrapper<T, W>>
        extends AbstractLambdaWrapper<T, W>
        implements CompareRequired<W, SFunction<T, ?>>, CompareIfNotEmpty<W, SFunction<T, ?>>, CompareColumn<W, SFunction<T, ?>> {

    /**
     * 是否需要查询
     */
    @Setter
    private Boolean needQuery;

    protected final W addConditionCol(boolean condition, SFunction<T, ?> column1, SqlKeyword sqlKeyword, SFunction<T, ?> column2) {
        return maybeDo(condition, () -> appendSqlSegments(columnToSqlSegment(column1), sqlKeyword,
                columnToSqlSegment(column2)));
    }

    @Override
    public W eqCol(boolean condition, SFunction<T, ?> column1, SFunction<T, ?> column2) {
        return addConditionCol(condition, column1, SqlKeyword.EQ, column2);
    }

    @Override
    public W neCol(boolean condition, SFunction<T, ?> column1, SFunction<T, ?> column2) {
        return addConditionCol(condition, column1, SqlKeyword.NE, column2);
    }

    @Override
    public W gtCol(boolean condition, SFunction<T, ?> column1, SFunction<T, ?> column2) {
        return addConditionCol(condition, column1, SqlKeyword.GT, column2);
    }

    @Override
    public W geCol(boolean condition, SFunction<T, ?> column1, SFunction<T, ?> column2) {
        return addConditionCol(condition, column1, SqlKeyword.GE, column2);
    }

    @Override
    public W ltCol(boolean condition, SFunction<T, ?> column1, SFunction<T, ?> column2) {
        return addConditionCol(condition, column1, SqlKeyword.LT, column2);
    }

    @Override
    public W leCol(boolean condition, SFunction<T, ?> column1, SFunction<T, ?> column2) {
        return addConditionCol(condition, column1, SqlKeyword.LE, column2);
    }
}
