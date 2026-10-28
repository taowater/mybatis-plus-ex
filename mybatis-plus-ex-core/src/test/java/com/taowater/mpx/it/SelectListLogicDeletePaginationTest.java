package com.taowater.mpx.it;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taowater.mpx.interceptor.ReturnTypeInterceptor;
import com.taowater.mpx.mapper.BaseMapper;
import com.taowater.mpx.method.ExMethodSqlInjector;
import lombok.Data;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 逻辑删除 + 条件分页：对齐 yellowear 报错场景（COUNT 仅剩 deleted=0 却仍绑定业务参数）。
 */
@EnabledForJreRange(min = JRE.JAVA_11)
class SelectListLogicDeletePaginationTest {

    private static JdbcDataSource dataSource;
    private static SqlSessionFactory factory;
    private static final List<String> BOUND_SQL_LOG = new ArrayList<>();

    private SqlSession session;
    private AccountMapper mapper;

    @BeforeAll
    static void bootstrap() {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:mpx_logic_page;DB_CLOSE_DELAY=-1;MODE=MySQL");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setEnvironment(new Environment("test", new JdbcTransactionFactory(), dataSource));
        configuration.setMapUnderscoreToCamelCase(true);

        GlobalConfig globalConfig = GlobalConfigUtils.defaults();
        globalConfig.setSqlInjector(new ExMethodSqlInjector(DbType.H2));
        globalConfig.setBanner(false);
        GlobalConfig.DbConfig dbConfig = globalConfig.getDbConfig();
        dbConfig.setLogicDeleteField("deleted");
        dbConfig.setLogicDeleteValue("1");
        dbConfig.setLogicNotDeleteValue("0");
        GlobalConfigUtils.setGlobalConfig(configuration, globalConfig);

        configuration.addInterceptor(new BoundSqlDumpInterceptor());
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.H2));
        configuration.addInterceptor(interceptor);
        configuration.addInterceptor(new ReturnTypeInterceptor());
        configuration.addMapper(AccountMapper.class);

        factory = new MybatisSqlSessionFactoryBuilder().build(configuration);
    }

    @BeforeEach
    void setUp() throws Exception {
        BOUND_SQL_LOG.clear();
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS t_account");
            st.execute("CREATE TABLE t_account ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "name VARCHAR(64), "
                    + "status INT, "
                    + "deleted INT DEFAULT 0)");
        }
        session = factory.openSession(true);
        mapper = session.getMapper(AccountMapper.class);
    }

    @AfterEach
    void tearDown() {
        if (session != null) {
            session.close();
        }
    }

    @Test
    void selectPage_logicDeletePlusEqAndOrderBy_shouldBindCountParams() throws Exception {
        Account active = new Account();
        active.setName("ok");
        active.setStatus(1);
        mapper.insert(active);

        Account other = new Account();
        other.setName("other");
        other.setStatus(2);
        mapper.insert(other);

        Account deleted = new Account();
        deleted.setName("deleted");
        deleted.setStatus(1);
        mapper.insert(deleted);
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute("UPDATE t_account SET deleted = 1 WHERE id = " + deleted.getId());
        }

        Page<Account> page = new Page<>(1, 10);
        try {
            Page<Account> result = mapper.selectPage(page, w -> w
                    .eq(Account::getStatus, 1)
                    .orderByDesc(Account::getId));
            assertThat(result.getTotal()).isEqualTo(1L);
            assertThat(result.getRecords()).extracting(Account::getName).containsExactly("ok");
        } catch (RuntimeException ex) {
            System.out.println("--- BoundSql dump ---");
            BOUND_SQL_LOG.forEach(System.out::println);
            throw ex;
        }
    }

    @Test
    void selectPage_logicDeletePlusEqX_shouldBindCountParams() throws Exception {
        Account active = new Account();
        active.setName("ok");
        active.setStatus(1);
        mapper.insert(active);

        Account deleted = new Account();
        deleted.setName("deleted");
        deleted.setStatus(1);
        mapper.insert(deleted);
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute("UPDATE t_account SET deleted = 1 WHERE id = " + deleted.getId());
        }

        Page<Account> page = new Page<>(1, 10);
        Page<Account> result = mapper.selectPage(page, w -> w
                .eqX(Account::getStatus, 1)
                .orderByDesc(Account::getId));

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getRecords()).hasSize(1);
    }

    @Intercepts({
            @Signature(type = Executor.class, method = "query", args = {
                    MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class
            }),
            @Signature(type = Executor.class, method = "query", args = {
                    MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class
            })
    })
    public static class BoundSqlDumpInterceptor implements Interceptor {
        @Override
        public Object intercept(Invocation invocation) throws Throwable {
            Object[] args = invocation.getArgs();
            MappedStatement ms = (MappedStatement) args[0];
            BoundSql boundSql = args.length == 6
                    ? (BoundSql) args[5]
                    : ms.getBoundSql(args[1]);
            long placeholders = boundSql.getSql().chars().filter(ch -> ch == '?').count();
            String props = boundSql.getParameterMappings().stream()
                    .map(ParameterMapping::getProperty)
                    .collect(Collectors.joining(","));
            BOUND_SQL_LOG.add(ms.getId() + " | ?=" + placeholders
                    + " map=" + boundSql.getParameterMappings().size()
                    + " [" + props + "] | " + boundSql.getSql().replace('\n', ' '));
            return invocation.proceed();
        }

        @Override
        public Object plugin(Object target) {
            return Plugin.wrap(target, this);
        }

        @Override
        public void setProperties(Properties properties) {
        }
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
