package com.taowater.mpx.fill;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SqlSetColumnsTest {

    @Test
    void readsOnlyAssignmentTargetsIncludingQuotedAndQualifiedColumns() {
        assertThat(SqlSetColumns.read("t.`updated_by`=concat('a,b=c', #{ew.paramNameValuePairs.p,jdbcType=VARCHAR}),"
                + "\"UPDATETIME\"=CURRENT_TIMESTAMP, [count]=[count]+1"))
                .containsExactly("updated_by", "updatetime", "count");
    }

    @Test
    void ignoresStringsAndCommentsAndHandlesTupleAssignments() {
        assertThat(SqlSetColumns.read("/* a,b=0 */ (\"a\",`b`)=('it''s,=x',func(1,2)),"
                + " c /* = fake, */ = CASE WHEN d=1 THEN 'x' ELSE 'y' END -- ignored=2\n"))
                .containsExactly("a", "b", "c");
    }

    @Test
    void preservesDotsWithinQuotedIdentifiersAndEscapedQuotes() {
        assertThat(SqlSetColumns.read("`a.b`=1, \"a\"\"b\"=2, [x]]y]=3"))
                .containsExactly("a.b", "a\"b", "x]y");
    }

    @Test
    void handlesEmptySetAndRejectsAmbiguousSyntaxRatherThanOverwriting() {
        assertThat(SqlSetColumns.read(null)).isEmpty();
        assertThat(SqlSetColumns.read(" ")).isEmpty();
        assertThatThrownBy(() -> SqlSetColumns.read("unknown_expression()=1"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SqlSetColumns.read("a=func(1,2"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SqlSetColumns.read("a='unterminated"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
