package com.taowater.mpx.filter;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.taowater.mpx.filter.op.Filter;
import com.taowater.mpx.wrapper.QueryExWrapper;
import com.taowater.taol.core.convert.GetSetHelper;
import com.taowater.taol.core.reflect.ReflectUtil;
import com.taowater.taol.core.util.EmptyUtil;
import com.taowater.ztream.Any;
import com.taowater.ztream.Ztream;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@UtilityClass
@SuppressWarnings("unchecked")
public class WrapperUtil {
    public static <T, P> Wrapper<T> getWrapper(Class<T> clazz, P param) {

        QueryExWrapper<T> wrapper = new QueryExWrapper<>(clazz);
        if (Objects.isNull(param)) {
            return wrapper;
        }
        Class<P> clazzParam = (Class<P>) param.getClass();

        TableInfo info = TableInfoHelper.getTableInfo(clazz);

        if (Objects.isNull(info)) {
            throw new IllegalStateException(
                    "No TableInfo for entity " + clazz.getName()
                            + "; ensure the class is a MyBatis-Plus entity (e.g. @TableName) and has been initialized");
        }

        // property -> column（含主键，主键通常不在 fieldList 中）
        Map<String, String> propertyColumnMap = new HashMap<>(Ztream.of(info.getFieldList())
                .toMap(TableFieldInfo::getProperty, TableFieldInfo::getColumn));
        if (StringUtils.isNotBlank(info.getKeyProperty())) {
            propertyColumnMap.put(info.getKeyProperty(), info.getKeyColumn());
        }

        Ztream.of(ReflectUtil.getFields(clazzParam)).forEach(paramField -> {
            Filter filterAnn = AnnotationUtil.getAnnotation(paramField, Filter.class);

            FilterStrategy filter = Any.of(filterAnn).get(Filter::filter, FilterStrategy.IGNORE_EMPTY);
            Operator operate = Any.of(filterAnn).get(Filter::operate, Operator.EQ);
            String column = Any.of(filterAnn).get(Filter::field, paramField.getName());
            // field 默认值为空串，此时回退到参数字段名
            if (EmptyUtil.isEmpty(column)) {
                column = paramField.getName();
            }

            // 忽略
            if (FilterStrategy.IGNORE.equals(filter)) {
                return;
            }
            String dbColumn = propertyColumnMap.get(column);

            if (Objects.isNull(dbColumn)) {
                return;
            }
            Function<P, Object> getter = GetSetHelper.buildGetter(clazzParam, paramField.getName());
            if (Objects.isNull(getter)) {
                return;
            }
            Object getValue = getter.apply(param);

            operate.getFun().apply(wrapper, filter.getPredicate().test(getValue), dbColumn, getValue);

            // 如果是空且符合短路策略
            if (FilterStrategy.RETURN_EMPTY_IF_EMPTY.equals(filter)) {
                if (EmptyUtil.isEmpty(getValue)) {
                    wrapper.setExecute(false);
                }
            }
        });
        return wrapper;
    }
}
