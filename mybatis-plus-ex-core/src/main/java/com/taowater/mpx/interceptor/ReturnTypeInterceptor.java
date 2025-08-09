package com.taowater.mpx.interceptor;

import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.taowater.mpx.constant.ExConstants;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * 用于实现动态resultType
 */
@SuppressWarnings("unchecked")
@Intercepts(@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}))
public class ReturnTypeInterceptor implements Interceptor {


    private static final Log log = LogFactory.getLog(ReturnTypeInterceptor.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object ew = null;
        Object[] args = invocation.getArgs();
        if (args[0] instanceof MappedStatement) {
            MappedStatement ms = (MappedStatement) args[0];
            if (args[1] instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) args[1];
                if (map.containsKey(Constants.WRAPPER)) {
                    ew = map.get(Constants.WRAPPER);
                }
                Class<?> rt = null;
                if (map.containsKey(ExConstants.RETURN_TYPE) && map.get(ExConstants.RETURN_TYPE) != null) {
                    rt = (Class<?>) map.get(ExConstants.RETURN_TYPE);
                }
                if (Objects.nonNull(rt)) {
                    args[0] = getMappedStatement(ms, rt, ew, map);
                }
            }
        }
        return invocation.proceed();
    }


    /**
     * 获取MappedStatement
     */
    public <E> MappedStatement getMappedStatement(MappedStatement ms, Class<?> resultType, Object ew, Map<String, Object> map) {

        return buildMappedStatement(ms, resultType, ew);
    }


    /**
     * 构建新的MappedStatement
     */
    private MappedStatement buildMappedStatement(MappedStatement ms, Class<?> resultType, Object ew) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), ms.getSqlSource(), ms.getSqlCommandType())
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
        String id = ms.getId() + StringPool.DOT + Constants.MYBATIS_PLUS + StringPool.UNDERSCORE + resultType.getName();
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
