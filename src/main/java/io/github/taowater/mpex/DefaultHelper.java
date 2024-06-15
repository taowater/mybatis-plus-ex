package io.github.taowater.mpex;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.taowater.ztream.Ztream;
import lombok.experimental.UtilityClass;
import org.dromara.hutool.core.map.MapUtil;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 默认值处理
 *
 * @author zhu56
 * @date 2023/09/02 19:59
 */
@UtilityClass
@SuppressWarnings("unchecked")
class DefaultHelper {

    /**
     * 建立对应类型的默认值映射
     */
    private static final Map<Predicate<Class<?>>, Supplier<Object>> MAP = MapUtil
            .builder(new HashMap<Predicate<Class<?>>, Supplier<Object>>())
            .put(List.class::isAssignableFrom, ArrayList::new)
            .put(Set.class::isAssignableFrom, HashSet::new)
            .put(Collection.class::isAssignableFrom, ArrayList::new)
            .put(Map.class::isAssignableFrom, HashMap::new)
            .put(IPage.class::isAssignableFrom, Page::new)
            .build();


    public <T> T getValue(Class<T> clazz) {
        return (T) Ztream.of(MAP.entrySet())
                .filter(e -> e.getKey().test(clazz))
                .first()
                .map(Map.Entry::getValue)
                .get(Supplier::get);
    }
}
