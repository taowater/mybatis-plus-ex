package com.taowater.mpx.wrapper.interfaces;

/**
 * 字段比较
 *
 * @author zhu56
 */
@SuppressWarnings("unused")
public interface CompareColumn<W, R> {

    /**
     * 字段1 = 字段2
     *
     * @param condition 条件
     * @param column1   字段1
     * @param column2   字段2
     * @return {@link W}
     */
    W eqCol(boolean condition, R column1, R column2);

    /**
     * 字段1 = 字段2
     *
     * @param column1 字段1
     * @param column2 字段2
     * @return {@link W}
     */
    default W eqCol(R column1, R column2) {
        return eqCol(true, column1, column2);
    }

    /**
     * 字段1 != 字段2
     *
     * @param condition 条件
     * @param column1   字段1
     * @param column2   字段2
     * @return {@link W}
     */
    W neCol(boolean condition, R column1, R column2);

    /**
     * 字段1 != 字段2
     *
     * @param column1 字段1
     * @param column2 字段2
     * @return {@link W}
     */
    default W neCol(R column1, R column2) {
        return neCol(true, column1, column2);

    }

    /**
     * 字段1 > 字段2
     *
     * @param condition 条件
     * @param column1   字段1
     * @param column2   字段2
     * @return {@link W}
     */
    W gtCol(boolean condition, R column1, R column2);

    /**
     * 字段1 > 字段2
     *
     * @param column1 字段1
     * @param column2 字段2
     * @return {@link W}
     */
    default W gtCol(R column1, R column2) {
        return gtCol(true, column1, column2);
    }

    /**
     * 字段1 >= 字段2
     *
     * @param condition 条件
     * @param column1   字段1
     * @param column2   字段2
     * @return {@link W}
     */
    W geCol(boolean condition, R column1, R column2);

    /**
     * 字段1 >= 字段2
     *
     * @param column1 字段1
     * @param column2 字段2
     * @return {@link W}
     */
    default W geCol(R column1, R column2) {
        return geCol(true, column1, column2);
    }

    /**
     * 字段1 < 字段2
     *
     * @param condition 条件
     * @param column1   字段1
     * @param column2   字段2
     * @return {@link W}
     */
    W ltCol(boolean condition, R column1, R column2);

    /**
     * 字段1 < 字段2
     *
     * @param column1 字段1
     * @param column2 字段2
     * @return {@link W}
     */
    default W ltCol(R column1, R column2) {
        return ltCol(true, column1, column2);
    }

    /**
     * 字段1 <= 字段2
     *
     * @param condition 条件
     * @param column1   字段1
     * @param column2   字段2
     * @return {@link W}
     */
    W leCol(boolean condition, R column1, R column2);

    /**
     * 字段1 <= 字段2
     *
     * @param column1 字段1
     * @param column2 字段2
     * @return {@link W}
     */
    default W leCol(R column1, R column2) {
        return leCol(true, column1, column2);
    }
}
