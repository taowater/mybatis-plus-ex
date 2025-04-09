package com.taowater.mpex;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.taowater.ztream.Ztream;
import org.apache.ibatis.session.Configuration;

import java.util.List;

class CustomSqlInjector extends DefaultSqlInjector {

    @Override
    public List<AbstractMethod> getMethodList(Configuration configuration, Class<?> mapperClass, TableInfo tableInfo) {
        List<AbstractMethod> methodList = super.getMethodList(configuration, mapperClass, tableInfo);
        // 2. 添加自定义方法
        return Ztream.of(methodList)
                .append(new SelectExists())
                .toList();
    }
}