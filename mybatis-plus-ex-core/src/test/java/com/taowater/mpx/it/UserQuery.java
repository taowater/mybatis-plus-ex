package com.taowater.mpx.it;

import com.taowater.mpx.filter.op.Gt;
import com.taowater.mpx.filter.op.In;
import com.taowater.mpx.filter.op.Like;
import lombok.Data;

import java.util.List;

/**
 * 注解驱动的查询 DTO，用于验证 {@link com.taowater.mpx.filter.WrapperUtil}。
 */
@Data
public class UserQuery {

    /**
     * 模糊匹配 name 列。
     */
    @Like
    private String name;

    /**
     * age &gt; minAge。
     */
    @Gt(field = "age")
    private Integer minAge;

    /**
     * 主键 IN；用于验证主键字段被纳入 property->column 映射。
     */
    @In(field = "id")
    private List<Long> ids;
}
