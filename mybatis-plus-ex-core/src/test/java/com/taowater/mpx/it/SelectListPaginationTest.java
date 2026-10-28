package com.taowater.mpx.it;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taowater.mpx.interceptor.ReturnTypeInterceptor;
import com.taowater.mpx.method.ExMethodSqlInjector;
import org.apache.ibatis.mapping.Environment;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证自定义 selectList（含 limit）与分页 COUNT 参数映射一致。
 */
@EnabledForJreRange(min = JRE.JAVA_11)
class SelectListPaginationTest {

    private static JdbcDataSource dataSource;
    private static SqlSessionFactory factory;

    private SqlSession session;
    private UserMapper mapper;

    @BeforeAll
    static void bootstrap() {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:mpx_page;DB_CLOSE_DELAY=-1;MODE=MySQL");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setEnvironment(new Environment("test", new JdbcTransactionFactory(), dataSource));
        configuration.setMapUnderscoreToCamelCase(true);

        GlobalConfig globalConfig = GlobalConfigUtils.defaults();
        globalConfig.setSqlInjector(new ExMethodSqlInjector(DbType.H2));
        globalConfig.setBanner(false);
        GlobalConfigUtils.setGlobalConfig(configuration, globalConfig);

        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.H2));
        configuration.addInterceptor(interceptor);
        configuration.addInterceptor(new ReturnTypeInterceptor());
        configuration.addMapper(UserMapper.class);

        factory = new MybatisSqlSessionFactoryBuilder().build(configuration);
    }

    @BeforeEach
    void setUp() throws Exception {
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS t_user");
            st.execute("CREATE TABLE t_user (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(64), age INT)");
        }
        session = factory.openSession(true);
        mapper = session.getMapper(UserMapper.class);
    }

    @AfterEach
    void tearDown() {
        if (session != null) {
            session.close();
        }
    }

    @Test
    void selectPage_withEqAndOrderBy_shouldNotFailOnCount() {
        User a = new User();
        a.setName("a");
        a.setAge(10);
        mapper.insert(a);
        User b = new User();
        b.setName("b");
        b.setAge(20);
        mapper.insert(b);

        Page<User> page = new Page<>(1, 10);
        Page<User> result = mapper.selectPage(page, w -> w
                .eq(User::getAge, 20)
                .orderByDesc(User::getId));

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getName()).isEqualTo("b");
    }

    @Test
    void selectPage_withEqX_shouldNotFailOnCount() {
        User a = new User();
        a.setName("a");
        a.setAge(10);
        mapper.insert(a);

        Page<User> page = new Page<>(1, 10);
        Page<User> result = mapper.selectPage(page, w -> w
                .eqX(User::getAge, 10)
                .orderByDesc(User::getId));

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getRecords()).extracting(User::getName).containsExactly("a");
    }
}
