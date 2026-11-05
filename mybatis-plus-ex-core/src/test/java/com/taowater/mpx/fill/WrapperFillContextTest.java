package com.taowater.mpx.fill;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WrapperFillContextTest {

    private static TableInfo tableInfo;

    @TableName("context_row")
    public static class ContextRow {
        @TableId
        public Long id;
        public Integer count;
        public String name;
        @TableField(updateStrategy = FieldStrategy.ALWAYS)
        public String always;
        @TableField(updateStrategy = FieldStrategy.NOT_EMPTY)
        public String nonEmpty;
        @TableField(updateStrategy = FieldStrategy.NEVER)
        public String never;
    }

    @BeforeAll
    static void metadata() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "context-fill-test");
        assistant.setCurrentNamespace("context-fill-test");
        tableInfo = TableInfoHelper.initTableInfo(assistant, ContextRow.class);
    }

    @Test
    void incrementExpressionsProtectTheColumnJustLikeManualSet() {
        WrapperFillContext context = WrapperFillContext.forUpdate(tableInfo, null, "count=count+1");
        context.fill("count", () -> { throw new AssertionError("A manually incremented field was filled"); });
        assertThat(context.isAssigned("count")).isTrue();
        assertThat(context.getValues()).isEmpty();
    }

    @Test
    void missingPrimaryKeyAndMismatchingTypesDoNotEvaluateSuppliers() {
        WrapperFillContext context = WrapperFillContext.forUpdate(tableInfo, null, null);
        context.fill("absent", () -> { throw new AssertionError(); });
        context.fill("id", () -> { throw new AssertionError(); });
        context.fill("name", Long.class, () -> { throw new AssertionError(); });
        assertThat(context.getValues()).isEmpty();
    }

    @Test
    void aNullDefaultAllowsTheNextRuleButTheFirstNonNullValueWins() {
        WrapperFillContext context = WrapperFillContext.forUpdate(tableInfo, null, null);
        context.fill("name", () -> null);
        context.fill("name", () -> "first");
        context.fill("name", () -> { throw new AssertionError("Duplicate default was evaluated"); });
        assertThat(context.getValues()).containsExactly("first");
    }

    @Test
    void entityUpdateStrategiesAreRespectedWhenCheckingAlreadyAssignedFields() {
        ContextRow entity = new ContextRow();
        entity.name = "manual";
        entity.nonEmpty = "";
        entity.never = "not-in-entity-set";
        WrapperFillContext context = WrapperFillContext.forUpdate(tableInfo, entity, null);
        assertThat(context.isAssigned("name")).isTrue();
        assertThat(context.isAssigned("always")).isTrue();
        assertThat(context.isAssigned("nonEmpty")).isFalse();
        assertThat(context.isAssigned("never")).isFalse();
    }
}
