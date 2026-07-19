package com.taowater.mpx.interceptor;

import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.taowater.mpx.constant.ExConstants;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * 用于实现动态 resultType
 */
@SuppressWarnings("unchecked")
@Intercepts(@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}))
public class ReturnTypeInterceptor implements Interceptor {

    /**
     * 缓存上限。(statementId + resultType) 组合在正常使用下有限，
     * 但为防御动态/异常场景无限增长，采用带容量上限的 LRU。
     */
    private static final int MAX_CACHE_SIZE = 1024;

    /**
     * 按 (statementId + resultType) 缓存已构建的 MappedStatement，避免每次查询重建。
     * <p>
     * 使用访问顺序的 LRU，并在超过 {@link #MAX_CACHE_SIZE} 时淘汰最久未用项，避免无界增长。
     */
    private final Map<String, MappedStatement> mappedStatementCache = Collections.synchronizedMap(
            new LinkedHashMap<String, MappedStatement>(256, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, MappedStatement> eldest) {
                    return size() > MAX_CACHE_SIZE;
                }
            });

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        if (!(args[0] instanceof MappedStatement)) {
            return invocation.proceed();
        }
        MappedStatement ms = (MappedStatement) args[0];
        if (!(args[1] instanceof Map)) {
            return invocation.proceed();
        }
        Map<String, Object> map = (Map<String, Object>) args[1];
        if (map.containsKey(ExConstants.RETURN_TYPE)) {
            Class<?> rt = (Class<?>) map.get(ExConstants.RETURN_TYPE);
            if (Objects.nonNull(rt)) {
                args[0] = mappedStatementCache.computeIfAbsent(
                        buildCacheKey(ms, rt),
                        k -> buildMappedStatement(ms, rt, k)
                );
            }
        }
        return invocation.proceed();
    }

    private static String buildCacheKey(MappedStatement ms, Class<?> resultType) {
        return ms.getId() + StringPool.DOT + Constants.MYBATIS_PLUS + StringPool.UNDERSCORE + resultType.getName();
    }

    /**
     * 构建带独立 id 与 resultMap 的 MappedStatement（不注册进 Configuration，仅替换本次查询参数）。
     */
    private MappedStatement buildMappedStatement(MappedStatement ms, Class<?> resultType, String id) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), id, ms.getSqlSource(), ms.getSqlCommandType())
                .resource(ms.getResource())
                .fetchSize(ms.getFetchSize())
                .statementType(ms.getStatementType())
                .keyGenerator(ms.getKeyGenerator())
                .timeout(ms.getTimeout())
                .parameterMap(ms.getParameterMap())
                .resultSetType(ms.getResultSetType())
                .cache(ms.getCache())
                .flushCacheRequired(ms.isFlushCacheRequired())
                .useCache(ms.isUseCache());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
            builder.keyProperty(String.join(StringPool.COMMA, ms.getKeyProperties()));
        }
        builder.resultMaps(Collections.singletonList(
                new ResultMap.Builder(ms.getConfiguration(), id, resultType, Collections.emptyList()).build()
        ));
        return builder.build();
    }

    @Override
    public Object plugin(Object target) {
        try {
            return Interceptor.super.plugin(target);
        } catch (Throwable e) {
            return Plugin.wrap(target, this);
        }
    }

    @Override
    public void setProperties(Properties properties) {
        try {
            Interceptor.super.setProperties(properties);
        } catch (Throwable ignored) {
        }
    }
}
