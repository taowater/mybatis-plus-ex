package com.taowater.mpx.fill;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/** A single update's defaults, separate from both the entity and the original Wrapper. */
public final class WrapperFillContext {

    public static final String PARAMETER = "__mpxWrapperFill";

    private final TableInfo tableInfo;
    private final Map<String, TableFieldInfo> fields = new LinkedHashMap<>();
    private final Set<String> assignedColumns;
    private final List<Object> values = new ArrayList<>();
    private final List<String> assignments = new ArrayList<>();

    private WrapperFillContext(TableInfo tableInfo, Object entity, String sqlSet) {
        this.tableInfo = Objects.requireNonNull(tableInfo, "tableInfo");
        this.assignedColumns = SqlSetColumns.read(sqlSet);
        for (TableFieldInfo field : tableInfo.getFieldList()) {
            if (field.isLogicDelete()) {
                continue;
            }
            fields.put(field.getProperty(), field);
            if (entity != null && participatesInEntitySet(field, entity)) {
                assignedColumns.add(SqlSetColumns.normalize(field.getColumn()));
            }
        }
    }

    public static WrapperFillContext forUpdate(TableInfo tableInfo, Object entity, String sqlSet) {
        return new WrapperFillContext(tableInfo, entity, sqlSet);
    }

    public Class<?> getEntityType() {
        return tableInfo.getEntityType();
    }

    /** Uses Java property names, not column names. Explicit SET NULL also counts as assigned. */
    public boolean isAssigned(String property) {
        TableFieldInfo field = fields.get(property);
        return field != null && assignedColumns.contains(SqlSetColumns.normalize(field.getColumn()));
    }

    /** The supplier is evaluated only for a mapped, unassigned property. Null results are ignored. */
    public WrapperFillContext fill(String property, Supplier<?> supplier) {
        Objects.requireNonNull(supplier, "supplier");
        TableFieldInfo field = fields.get(property);
        if (field == null || isAssigned(property)) {
            return this;
        }
        Object value = supplier.get();
        if (value == null) {
            return this;
        }
        String mapping = field.getMapping();
        String parameter = PARAMETER + ".values[" + values.size() + "]";
        assignments.add(field.getColumn() + "=#{" + parameter
                + (mapping == null || mapping.isEmpty() ? "" : "," + mapping) + "}");
        values.add(value);
        assignedColumns.add(SqlSetColumns.normalize(field.getColumn()));
        return this;
    }

    /** Like strict filling, optionally restrict a rule to properties of exactly the requested type. */
    public <V> WrapperFillContext fill(String property, Class<V> fieldType, Supplier<? extends V> supplier) {
        TableFieldInfo field = fields.get(property);
        if (field != null && field.getPropertyType().equals(fieldType)) {
            fill(property, supplier);
        }
        return this;
    }

    public String getSqlSet() {
        return String.join(",", assignments);
    }

    public List<Object> getValues() {
        return Collections.unmodifiableList(values);
    }

    private boolean participatesInEntitySet(TableFieldInfo field, Object entity) {
        if (field.isWithUpdateFill()) {
            return true;
        }
        FieldStrategy strategy = field.getUpdateStrategy();
        if (strategy == FieldStrategy.NEVER) {
            return false;
        }
        if (field.getPropertyType().isPrimitive() || strategy == FieldStrategy.ALWAYS) {
            return true;
        }
        Object value = tableInfo.getPropertyValue(entity, field.getProperty());
        return value != null && !(strategy == FieldStrategy.NOT_EMPTY
                && value instanceof CharSequence && ((CharSequence) value).length() == 0);
    }
}
