package com.taowater.mpx.it;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taowater.mpx.mapper.BaseMapper;
import com.taowater.mpx.method.ExMethodSqlInjector;
import com.taowater.mpx.wrapper.LambdaQueryExWrapper;
import lombok.Data;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class BoundSqlLogicDeleteDumpTest {

    @Test
    void dumpWithAndWithoutPage() throws Exception {
        dump(true);
        System.out.println("======== DEFAULT ========");
        dump(false);
    }

    private void dump(boolean useEx) throws Exception {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:dump" + (useEx ? "ex" : "def") + ";DB_CLOSE_DELAY=-1;MODE=MySQL");
        ds.setUser("sa");
        ds.setPassword("");
        MybatisConfiguration cfg = new MybatisConfiguration();
        cfg.setEnvironment(new Environment("t", new JdbcTransactionFactory(), ds));
        GlobalConfig gc = GlobalConfigUtils.defaults();
        gc.setSqlInjector(useEx ? new ExMethodSqlInjector(DbType.H2)
                : new com.baomidou.mybatisplus.core.injector.DefaultSqlInjector());
        gc.setBanner(false);
        gc.getDbConfig().setLogicDeleteField("deleted");
        gc.getDbConfig().setLogicDeleteValue("1");
        gc.getDbConfig().setLogicNotDeleteValue("0");
        GlobalConfigUtils.setGlobalConfig(cfg, gc);
        cfg.addMapper(AccountMapper.class);
        new MybatisSqlSessionFactoryBuilder().build(cfg);
        LambdaQueryExWrapper<Account> w = new LambdaQueryExWrapper<>(Account.class);
        w.eq(Account::getStatus, 1).orderByDesc(Account::getId);
        Page<Account> page = new Page<>(1, 10);
        Map<String, Object> pageParam = new HashMap<>();
        pageParam.put("param1", page);
        pageParam.put(Constants.WRAPPER, w);
        MappedStatement ms = cfg.getMappedStatement(AccountMapper.class.getName() + ".selectList");
        BoundSql pageBs = ms.getBoundSql(pageParam);
        String label = useEx ? "EX" : "DEF";
        System.out.println(label + "_SQL=[" + pageBs.getSql().replace('\n', ' ') + "]");
        System.out.println(label + "_Q=" + pageBs.getSql().chars().filter(c -> c == '?').count()
                + " MAP=" + pageBs.getParameterMappings().size());
        PaginationInnerInterceptor pager = new PaginationInnerInterceptor(DbType.H2);
        String countSql = pager.autoCountSql(page, pageBs.getSql());
        System.out.println(label + "_COUNT=[" + countSql + "]");
        System.out.println(label + "_COUNT_Q=" + countSql.chars().filter(c -> c == '?').count());
    }

    @Data
    @TableName("t_account")
    public static class Account {
        @TableId(type = IdType.AUTO)
        private Long id;
        private String name;
        private Integer status;
        @TableLogic
        private Integer deleted;
    }

    public interface AccountMapper extends BaseMapper<Account> {
    }
}
