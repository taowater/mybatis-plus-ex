package com.taowater.mpx.filter;

import com.taowater.mpx.filter.op.Filter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 字段上的过滤声明（拆开读）：
 * <ul>
 *   <li>{@code operate}：注解「类型」上的元注解 {@link Filter}（字段直接标 {@code @Filter} 时从实例读取）</li>
 *   <li>{@code field} / {@code filter}：外层注解实例上的同名属性</li>
 * </ul>
 * 按注解类型缓存元信息与 Method，避免重复反射。
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class FilterAnn {

    private final Operator operate;
    private final String field;
    private final FilterStrategy strategy;

    private static final Object ABSENT = new Object();
    private static final ConcurrentHashMap<Class<? extends Annotation>, Object> TYPE_CACHE = new ConcurrentHashMap<>();

    /**
     * 解析元素上第一个可用的过滤注解；无则返回 {@code null}。
     */
    static FilterAnn resolve(AnnotatedElement el) {
        for (Annotation ann : el.getAnnotations()) {
            FilterAnn resolved = from(ann);
            if (resolved != null) {
                return resolved;
            }
        }
        return null;
    }

    static FilterAnn from(Annotation ann) {
        TypeMeta meta = typeMeta(ann.annotationType());
        if (meta == null) {
            return null;
        }
        Operator operate = meta.operateFromInstance ? ((Filter) ann).operate() : meta.operate;
        String field = invoke(meta.fieldMethod, ann, "");
        FilterStrategy strategy = invoke(meta.filterMethod, ann, FilterStrategy.IGNORE_EMPTY);
        return new FilterAnn(operate, field, strategy);
    }

    @SuppressWarnings("unchecked")
    private static TypeMeta typeMeta(Class<? extends Annotation> type) {
        Object cached = TYPE_CACHE.computeIfAbsent(type, t -> {
            TypeMeta built = buildTypeMeta((Class<? extends Annotation>) t);
            return built == null ? ABSENT : built;
        });
        return cached == ABSENT ? null : (TypeMeta) cached;
    }

    private static TypeMeta buildTypeMeta(Class<? extends Annotation> type) {
        if (Filter.class.equals(type)) {
            return new TypeMeta(null, true, method(type, "field"), method(type, "filter"));
        }
        Filter meta = type.getAnnotation(Filter.class);
        if (meta == null) {
            return null;
        }
        return new TypeMeta(meta.operate(), false, method(type, "field"), method(type, "filter"));
    }

    private static Method method(Class<? extends Annotation> type, String name) {
        try {
            Method m = type.getMethod(name);
            m.setAccessible(true);
            return m;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T invoke(Method method, Annotation ann, T defaultValue) {
        if (method == null) {
            return defaultValue;
        }
        try {
            Object value = method.invoke(ann);
            return value == null ? defaultValue : (T) value;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to read @" + ann.annotationType().getSimpleName()
                    + "." + method.getName(), e);
        }
    }

    @RequiredArgsConstructor
    private static final class TypeMeta {
        /** 组合注解上元注解给出的 operate；{@link Filter} 自身为 null */
        private final Operator operate;
        private final boolean operateFromInstance;
        private final Method fieldMethod;
        private final Method filterMethod;
    }
}
