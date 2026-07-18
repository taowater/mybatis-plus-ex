package com.taowater.mpx.spring.entity;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

/**
 * JDK 8 冒烟：{@code JAVA_HOME=jdk8 mvn -pl mybatis-plus-ex-spring -am test-compile}
 * 然后用测试 classpath 运行本类 main。
 */
public final class GenerateHelperSmoke {

    private GenerateHelperSmoke() {
    }

    public static void main(String[] args) throws Exception {
        ClassLoadingStrategy<? super ClassLoader> strategy =
                GenerateHelper.resolveStrategy(generated.service.PackageAnchor.class);
        boolean lookup = ClassInjector.UsingLookup.isAvailable();
        if (lookup && !(strategy instanceof ClassLoadingStrategy.UsingLookup)) {
            throw new IllegalStateException("expected UsingLookup on Java 9+");
        }
        if (!lookup && strategy != ClassLoadingStrategy.Default.INJECTION) {
            throw new IllegalStateException("expected INJECTION on Java 8");
        }

        String name = "generated.service.GenerateHelperSmokeDynamic";
        Class<?> loaded = GenerateHelper.loadService(new ByteBuddy()
                .subclass(Object.class)
                .name(name)
                .make());
        if (loaded.getClassLoader() != generated.service.PackageAnchor.class.getClassLoader()) {
            throw new IllegalStateException("dynamic class not in app ClassLoader");
        }
        Class.forName(name, false, loaded.getClassLoader());
        System.out.println("GenerateHelperSmoke OK, java="
                + System.getProperty("java.version")
                + ", strategy="
                + (lookup ? "UsingLookup" : "INJECTION"));
    }
}
