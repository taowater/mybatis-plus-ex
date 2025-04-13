package com.taowater.mpex.method;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.taowater.ztream.Ztream;
import lombok.NoArgsConstructor;
import org.apache.ibatis.session.Configuration;

import java.util.List;

@NoArgsConstructor
public class ExMethodSqlInjector extends DefaultSqlInjector {

    private DbType dbType;

    public ExMethodSqlInjector(DbType dbType) {
        this.dbType = dbType;
    }

    @Override
    public List<AbstractMethod> getMethodList(Configuration configuration, Class<?> mapperClass, TableInfo tableInfo) {
        List<AbstractMethod> methodList = super.getMethodList(configuration, mapperClass, tableInfo);
        // 2. 添加自定义方法
        return Ztream.of(methodList)
                .append(new SelectExists())
                .append(new SelectLimit(dbType))
                .toList();
    }
}