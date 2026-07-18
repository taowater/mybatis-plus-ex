package generated.repository;

import java.lang.invoke.MethodHandles;

/**
 * ByteBuddy UsingLookup 锚点：动态 Repository 与本类同包，才能注入到应用 ClassLoader。
 */
public final class PackageAnchor {
    private PackageAnchor() {
    }

    /**
     * caller-sensitive：lookupClass 为本类，供同包 {@code defineClass} 使用。
     */
    public static MethodHandles.Lookup lookup() {
        return MethodHandles.lookup();
    }
}
