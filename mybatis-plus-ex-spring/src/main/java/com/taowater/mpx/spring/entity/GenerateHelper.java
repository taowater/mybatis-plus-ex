package com.taowater.mpx.spring.entity;

import lombok.experimental.UtilityClass;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

/**
 * 动态生成
 *
 * @author zhu56
 * @date 2025/04/26 00:18
 */
@UtilityClass
public class GenerateHelper {

    /**
     * 动态类全名模板：{@code generated.{kind}.{unique}Dynamic{suffix}}。
     * {@code unique} 须能区分不同实体（勿仅用 simpleName）。
     */
    public final static String template = "generated.%s.%sDynamic%s";

    /**
     * 生成唯一动态类名。使用实体 FQN（{@code .} → {@code _}）避免不同包同 simpleName 碰撞，
     * 并保持落在 {@code generated.mapper|repository|service} 包内以便 PackageAnchor Lookup。
     */
    public static String className(String kind, Class<?> entityClass, String suffix) {
        String unique = entityClass.getName().replace('.', '_');
        return String.format(template, kind, unique, suffix);
    }

    /**
     * 加载动态 Mapper（注入到应用 ClassLoader）。
     */
    public static Class<?> loadMapper(DynamicType.Unloaded<?> unloaded) {
        return load(unloaded, generated.mapper.PackageAnchor.class);
    }

    /**
     * 加载动态 Repository。
     */
    public static Class<?> loadRepository(DynamicType.Unloaded<?> unloaded) {
        return load(unloaded, generated.repository.PackageAnchor.class);
    }

    /**
     * 加载动态 Service（含 history-data 的 ServiceGenerator）。
     */
    public static Class<?> loadService(DynamicType.Unloaded<?> unloaded) {
        return load(unloaded, generated.service.PackageAnchor.class);
    }

    /**
     * 将动态类注入到与 {@code packageNeighbor} 相同的 ClassLoader / 包中。
     * <ul>
     *   <li>Java 9+（{@link ClassInjector.UsingLookup#isAvailable()}）：包内锚点 Lookup + UsingLookup</li>
     *   <li>Java 8：{@link ClassLoadingStrategy.Default#INJECTION}（依赖 Unsafe，Java 8 可用）</li>
     * </ul>
     * 不使用 WRAPPER：子 ClassLoader 无法被 Spring CGLIB / {@code Class.forName} 正确处理。
     */
    public static Class<?> load(DynamicType.Unloaded<?> unloaded, Class<?> packageNeighbor) {
        ClassLoader classLoader = packageNeighbor.getClassLoader();
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        ClassLoadingStrategy<? super ClassLoader> strategy = resolveStrategy(packageNeighbor);
        return unloaded.load(classLoader, strategy).getLoaded();
    }

    /**
     * 当前 JVM 选用的加载策略（便于排查 / 测试断言）。
     */
    static ClassLoadingStrategy<? super ClassLoader> resolveStrategy(Class<?> packageNeighbor) {
        if (ClassInjector.UsingLookup.isAvailable()) {
            try {
                Object lookup = packageNeighbor.getMethod("lookup").invoke(null);
                return ClassLoadingStrategy.UsingLookup.of(lookup);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(
                        "UsingLookup is available but package neighbor lookup() failed: " + packageNeighbor.getName(),
                        e
                );
            }
        }
        // Java 8：Lookup.defineClass 不可用，走同 ClassLoader 的 Unsafe 注入
        return ClassLoadingStrategy.Default.INJECTION;
    }
}
