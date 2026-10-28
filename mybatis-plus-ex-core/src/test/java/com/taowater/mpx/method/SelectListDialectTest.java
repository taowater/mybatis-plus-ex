package com.taowater.mpx.method;

import com.baomidou.mybatisplus.annotation.DbType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SelectListDialectTest {

    @Test
    void sqlServer2005_shouldUseTopSyntax() {
        String sql = new SelectList(DbType.SQL_SERVER2005).sqlLimit(DbType.SQL_SERVER2005);

        assertThat(sql).contains("TOP ${ew.limit}").doesNotContain("LIMIT ${ew.limit}");
    }

    @Test
    void db2_shouldUseFetchFirstSyntax() {
        assertThat(new SelectList(DbType.DB2).sqlLimit(DbType.DB2))
                .contains("FETCH FIRST ${ew.limit} ROWS ONLY");
    }
}
