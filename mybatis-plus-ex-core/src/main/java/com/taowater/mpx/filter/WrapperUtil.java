package com.taowater.mpx.filter;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.taowater.mpx.wrapper.QueryExWrapper;
import com.taowater.taol.core.convert.GetSetHelper;
import com.taowater.taol.core.reflect.ReflectUtil;
import com.taowater.taol.core.util.EmptyUtil;
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
            FilterAnn ann = FilterAnn.resolve(paramField);
            if (ann == null) {
                return;
            }

            FilterStrategy strategy = ann.getStrategy();
            if (FilterStrategy.IGNORE.equals(strategy)) {
                return;
            }

            String column = ann.getField();
            if (EmptyUtil.isEmpty(column)) {
                column = paramField.getName();
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

            ann.getOperate().getFun().apply(wrapper, strategy.getPredicate().test(getValue), dbColumn, getValue);

            if (FilterStrategy.RETURN_EMPTY_IF_EMPTY.equals(strategy) && EmptyUtil.isEmpty(getValue)) {
                wrapper.setExecute(false);
            }
        });
        return wrapper;
    }
}
