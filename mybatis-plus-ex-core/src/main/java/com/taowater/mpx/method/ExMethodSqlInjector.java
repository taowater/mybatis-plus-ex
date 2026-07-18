package com.taowater.mpx.method;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import com.taowater.ztream.Ztream;
import lombok.NoArgsConstructor;
import org.apache.ibatis.session.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * @author zhu56
 */
@NoArgsConstructor
@SuppressWarnings("unused")
public class ExMethodSqlInjector extends DefaultSqlInjector {

    private volatile DbType dbType;

    public ExMethodSqlInjector(DbType dbType) {
        this.dbType = dbType;
    }

    @Override
    public List<AbstractMethod> getMethodList(Configuration configuration, Class<?> mapperClass, TableInfo tableInfo) {
        List<AbstractMethod> methodList = super.getMethodList(configuration, mapperClass, tableInfo);
        // 移除原来的selectList方法
        methodList.removeIf(e -> e instanceof com.baomidou.mybatisplus.core.injector.methods.SelectList);
        // 2. 添加自定义方法（DbType 仅探测一次并缓存，避免按表借连接）
        DbType type = resolveDbType(configuration);
        return Ztream.of(methodList)
                .append(new SelectExists(type))
                .append(new SelectList(type))
                .toList();
    }

    private DbType resolveDbType(Configuration configuration) {
        DbType type = this.dbType;
        if (type != null) {
            return type;
        }
        synchronized (this) {
            if (this.dbType != null) {
                return this.dbType;
            }
            Connection connection = null;
            try {
                DataSource dataSource = configuration.getEnvironment().getDataSource();
                connection = dataSource.getConnection();
                this.dbType = JdbcUtils.getDbType(connection.getMetaData().getURL());
                return this.dbType;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to determine database type", e);
            } finally {
                if (connection != null) {
                    try {
                        if (!connection.isClosed()) {
                            connection.close();
                        }
                    } catch (SQLException ignored) {
                    }
                }
            }
        }
    }
}
