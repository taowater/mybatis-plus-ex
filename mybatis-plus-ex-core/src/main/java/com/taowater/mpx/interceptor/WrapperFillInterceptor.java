package com.taowater.mpx.interceptor;

import com.baomidou.mybatisplus.core.conditions.update.Update;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.taowater.mpx.fill.WrapperFillContext;
import com.taowater.mpx.fill.WrapperFillHandler;
import com.taowater.mpx.fill.WrapperFillSqlSource;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Supplies defaults before dynamic SQL is generated, without mutating the caller's Wrapper. */
@Intercepts(@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}))
public class WrapperFillInterceptor implements Interceptor {

    private final List<WrapperFillHandler> handlers;

    public WrapperFillInterceptor(WrapperFillHandler... handlers) {
        this(Arrays.asList(handlers));
    }

    public WrapperFillInterceptor(Collection<? extends WrapperFillHandler> handlers) {
        this.handlers = new ArrayList<>(Objects.requireNonNull(handlers, "handlers"));
        this.handlers.forEach(handler -> Objects.requireNonNull(handler, "handler"));
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement statement = (MappedStatement) args[0];
        if (handlers.isEmpty() || !(statement.getSqlSource() instanceof WrapperFillSqlSource)
                || !(args[1] instanceof Map)) {
            return invocation.proceed();
        }
        Map<?, ?> original = (Map<?, ?>) args[1];
        Object wrapper = original.containsKey(Constants.WRAPPER) ? original.get(Constants.WRAPPER) : null;
        if (!(wrapper instanceof Update)) {
            return invocation.proceed();
        }
        WrapperFillSqlSource source = (WrapperFillSqlSource) statement.getSqlSource();
        Object entity = original.containsKey(Constants.ENTITY) ? original.get(Constants.ENTITY) : null;
        WrapperFillContext context = WrapperFillContext.forUpdate(
                source.getTableInfo(), entity, ((Update<?, ?>) wrapper).getSqlSet());
        handlers.forEach(handler -> handler.updateFill(context));
        if (context.getValues().isEmpty()) {
            return invocation.proceed();
        }
        Map<Object, Object> parameters = new LinkedHashMap<>(original);
        parameters.put(WrapperFillContext.PARAMETER, context);
        args[1] = parameters;
        try {
            return invocation.proceed();
        } finally {
            args[1] = original;
        }
    }
}
