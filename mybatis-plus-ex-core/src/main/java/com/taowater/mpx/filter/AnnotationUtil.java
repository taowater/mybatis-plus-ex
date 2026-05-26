package com.taowater.mpx.filter;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class AnnotationUtil {

    /**
     * 获取注解对象（直接或间接），支持跨注解 @AliasFor
     */
    public static <A extends Annotation> A getAnnotation(AnnotatedElement el, Class<A> annotationType) {
        // 1. 找直接注解
        Annotation direct = el.getAnnotation(annotationType);

        // 2. 扫描元注解（间接注解）
        Annotation candidate = null;
        for (Annotation ann : el.getAnnotations()) {
            if (ann.annotationType().getAnnotation(annotationType) != null) {
                candidate = ann;
                break;
            }
        }

        if (direct == null && candidate == null) {
            return null;
        }

        Annotation source = direct != null ? direct : candidate;

        // 3. 收集原始属性
        Map<String, Object> attributes = new HashMap<>();
        for (Method m : source.annotationType().getDeclaredMethods()) {
            try {
                attributes.put(m.getName(), m.invoke(source));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


        // 4. 处理跨注解 alias
        processAlias(el, annotationType, attributes);

        // 5. 动态代理返回最终注解对象
        Object proxy = Proxy.newProxyInstance(annotationType.getClassLoader(),
                new Class[]{annotationType},
                new AnnotationInvocationHandler(annotationType, attributes));

        return annotationType.cast(proxy);
    }

    private static void processAlias(AnnotatedElement el, Class<? extends Annotation> targetType,
                                     Map<String, Object> attributes) {
        for (Annotation ann : el.getAnnotations()) {
            Class<? extends Annotation> annType = ann.annotationType();
            for (Method m : annType.getDeclaredMethods()) {
                AliasFor alias = m.getAnnotation(AliasFor.class);
                if (alias == null) continue;

                if (alias.annotation() != targetType) continue;

                // 获取源注解值
                Object value = null;
                try {
                    value = m.invoke(ann);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                // 只有显式值才覆盖
                Object defaultVal = m.getDefaultValue();
                if (value != null && !value.equals(defaultVal)) {
                    attributes.put(alias.value(), value);
                }
            }
        }
    }

    /**
     * 动态代理实现访问注解属性
     */
    private static class AnnotationInvocationHandler implements InvocationHandler {
        private final Class<? extends Annotation> type;
        private final Map<String, Object> attributes;

        public AnnotationInvocationHandler(Class<? extends Annotation> type, Map<String, Object> attributes) {
            this.type = type;
            this.attributes = attributes;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if ("annotationType".equals(name)) return type;
            if (attributes.containsKey(name)) return attributes.get(name);
            return method.getDefaultValue();
        }
    }
}
