package com.taowater.mpx.it;

import lombok.Data;

/**
 * 投影 DTO，用于验证动态 resultType（{@link com.taowater.mpx.interceptor.ReturnTypeInterceptor}）。
 */
@Data
public class NameOnly {
    private String name;
}
