/*
 * Copyright (c) 2011-2025, baomidou (jobob@qq.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.taowater.mpx.method;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 拓展方法
 *
 * @author zhu56
 */
@Getter
@AllArgsConstructor
enum SqlExMethod {
    /**
     * 判断条件数据是否存在
     */
    SELECT_EXISTS("selectExists", "判断是否存在", "<script>SELECT 1 WHERE EXISTS (SELECT 1 FROM %s %s)\n</script>"),
    /**
     * 拓展limit的选择
     */
    SELECT_LIST("selectList", "查询满足条件所有数据", "<script>%s SELECT %s FROM %s %s %s %s %s\n</script>"),

    ;
    private final String method;
    private final String desc;
    private final String sql;

}
