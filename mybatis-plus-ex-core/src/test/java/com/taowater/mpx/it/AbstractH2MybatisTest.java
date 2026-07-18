package com.taowater.mpx.it;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
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

import java.sql.Connection;
import java.sql.Statement;

/**
 * H2 + MyBatis-Plus 独立启动基建（无 Spring）。
 */
abstract class AbstractH2MybatisTest {

    protected static JdbcDataSource dataSource;
    protected static SqlSessionFactory factory;

    protected SqlSession session;
    protected UserMapper mapper;

    @BeforeAll
    static void bootstrap() {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:mpx_it;DB_CLOSE_DELAY=-1;MODE=MySQL");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setEnvironment(new Environment("test", new JdbcTransactionFactory(), dataSource));
        configuration.setMapUnderscoreToCamelCase(true);

        GlobalConfig globalConfig = GlobalConfigUtils.defaults();
        globalConfig.setSqlInjector(new ExMethodSqlInjector(DbType.H2));
        globalConfig.setBanner(false);
        GlobalConfigUtils.setGlobalConfig(configuration, globalConfig);

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

    protected User newUser(String name, int age) {
        User u = new User();
        u.setName(name);
        u.setAge(age);
        mapper.insert(u);
        return u;
    }
}
