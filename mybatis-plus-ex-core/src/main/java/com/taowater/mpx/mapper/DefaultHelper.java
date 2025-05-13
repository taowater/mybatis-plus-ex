package com.taowater.mpx.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taowater.ztream.Ztream;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 默认值处理
 *
 * @author zhu56
 */
@UtilityClass
@SuppressWarnings("unchecked")
class DefaultHelper {

    /**
     * 建立对应类型的默认值映射
     */
    private static final Map<Predicate<Class<?>>, Supplier<Object>> MAP = new LinkedHashMap<>();

    static {
        MAP.put(List.class::isAssignableFrom, ArrayList::new);
        MAP.put(Set.class::isAssignableFrom, HashSet::new);
        MAP.put(Collection.class::isAssignableFrom, ArrayList::new);
        MAP.put(Map.class::isAssignableFrom, HashMap::new);
        MAP.put(IPage.class::isAssignableFrom, Page::new);
    }


    public <T> T getValue(Class<T> clazz) {
        return (T) Ztream.of(MAP.entrySet())
                .filter(e -> e.getKey().test(clazz))
                .first()
                .map(Map.Entry::getValue)
                .get(Supplier::get);
    }
}
