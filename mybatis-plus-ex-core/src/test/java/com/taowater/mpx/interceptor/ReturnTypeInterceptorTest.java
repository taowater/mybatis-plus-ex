package com.taowater.mpx.interceptor;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Signature;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ReturnTypeInterceptorTest {

    @Test
    void shouldInterceptBothExecutorQueryOverloads() {
        Intercepts intercepts = ReturnTypeInterceptor.class.getAnnotation(Intercepts.class);
        assertThat(intercepts).isNotNull();
        assertThat(Arrays.stream(intercepts.value())
                .filter(signature -> signature.type() == Executor.class)
                .filter(signature -> "query".equals(signature.method()))
                .map(Signature::args)
                .mapToInt(args -> args.length)
                .boxed())
                .containsExactlyInAnyOrder(4, 6);
    }
}
