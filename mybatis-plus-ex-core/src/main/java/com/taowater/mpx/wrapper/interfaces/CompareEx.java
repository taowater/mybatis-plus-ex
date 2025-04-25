package com.taowater.mpx.wrapper.interfaces;

import com.baomidou.mybatisplus.core.enums.SqlKeyword;

/**
 * 比较拓展
 *
 * @author zhu56
 * @date 2025/04/22 21:51
 */
public interface CompareEx<W, R> extends CompareRequired<W, R>, CompareIfNotEmpty<W, R>, CompareColumn<W, R> {

    W addConditionCol(boolean condition, R column1, SqlKeyword sqlKeyword, R column2);


    @Override
    default W eqCol(boolean condition, R column1, R column2) {
        return addConditionCol(condition, column1, SqlKeyword.EQ, column2);
    }

    @Override
    default W neCol(boolean condition, R column1, R column2) {
        return addConditionCol(condition, column1, SqlKeyword.NE, column2);
    }

    @Override
    default W gtCol(boolean condition, R column1, R column2) {
        return addConditionCol(condition, column1, SqlKeyword.GT, column2);
    }

    @Override
    default W geCol(boolean condition, R column1, R column2) {
        return addConditionCol(condition, column1, SqlKeyword.GE, column2);
    }

    @Override
    default W ltCol(boolean condition, R column1, R column2) {
        return addConditionCol(condition, column1, SqlKeyword.LT, column2);
    }

    @Override
    default W leCol(boolean condition, R column1, R column2) {
        return addConditionCol(condition, column1, SqlKeyword.LE, column2);
    }

}
