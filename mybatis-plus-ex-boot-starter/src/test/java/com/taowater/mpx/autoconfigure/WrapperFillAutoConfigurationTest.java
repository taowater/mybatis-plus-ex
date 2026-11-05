package com.taowater.mpx.autoconfigure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import com.taowater.mpx.fill.WrapperFillHandler;
import com.taowater.mpx.interceptor.WrapperFillInterceptor;
import com.taowater.mpx.method.ExMethodSqlInjector;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WrapperFillAutoConfigurationTest {

    @TableName("boot_fill_row")
    public static class BootRow {
        @TableId
        public Long id;
        public String name;
        public String operator;
    }

    public interface BootMapper extends com.baomidou.mybatisplus.core.mapper.BaseMapper<BootRow> {
    }

    @Configuration(proxyBeanMethods = false)
    public static class SessionFactoryConfiguration {
        @Bean
        public SqlSessionFactory sqlSessionFactory(DataSource dataSource, ExMethodSqlInjector injector,
                                                   List<Interceptor> interceptors) throws Exception {
            MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
            factory.setDataSource(dataSource);
            GlobalConfig config = GlobalConfigUtils.defaults();
            config.setBanner(false);
            config.setSqlInjector(injector);
            factory.setGlobalConfig(config);
            factory.setPlugins(interceptors.toArray(new Interceptor[0]));
            return factory.getObject();
        }
    }

    @Test
    void aSingleHandlerBeanFillsUpdatesThroughASpringManagedSqlSessionFactory() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:boot_fill_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        try (Connection connection = dataSource.getConnection(); Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE boot_fill_row(id BIGINT PRIMARY KEY,name VARCHAR(30),operator VARCHAR(30))");
            st.execute("INSERT INTO boot_fill_row VALUES(1,'old','old-operator')");
        }
        try (AnnotationConfigApplicationContext context = context()) {
            context.registerBean(DataSource.class, () -> dataSource);
            context.registerBean("defaults", WrapperFillHandler.class,
                    () -> fill -> fill.fill("operator", () -> "auto-operator"));
            context.register(MyBatisPlusExConfig.class, SessionFactoryConfiguration.class);
            context.refresh();
            SqlSessionFactory factory = context.getBean(SqlSessionFactory.class);
            factory.getConfiguration().addMapper(BootMapper.class);
            try (SqlSession session = factory.openSession(true)) {
                BootMapper mapper = session.getMapper(BootMapper.class);
                assertEquals(1, mapper.update(new UpdateWrapper<BootRow>().eq("id", 1L).set("name", "new")));
                assertEquals("auto-operator", mapper.selectById(1L).operator);
                mapper.update(new UpdateWrapper<BootRow>().eq("id", 1L).set("operator", null));
                assertEquals(null, mapper.selectById(1L).operator);
            }
        }
    }

    @Test
    void oneHandlerBeanAutomaticallyRegistersTheInterceptor() {
        try (AnnotationConfigApplicationContext context = context()) {
            context.registerBean("defaults", WrapperFillHandler.class, () -> fill -> { });
            context.register(MyBatisPlusExConfig.class);
            context.refresh();
            assertEquals(1, context.getBeansOfType(WrapperFillInterceptor.class).size());
        }
    }

    @Test
    void noHandlerDoesNotRegisterAnInterceptor() {
        try (AnnotationConfigApplicationContext context = context()) {
            context.register(MyBatisPlusExConfig.class);
            context.refresh();
            assertTrue(context.getBeansOfType(WrapperFillInterceptor.class).isEmpty());
        }
    }

    @Test
    void aUserProvidedInterceptorIsPreservedAndMultipleHandlersAreAccepted() {
        try (AnnotationConfigApplicationContext context = context()) {
            WrapperFillHandler first = fill -> { };
            WrapperFillHandler second = fill -> { };
            WrapperFillInterceptor custom = new WrapperFillInterceptor(first, second);
            context.registerBean("first", WrapperFillHandler.class, () -> first);
            context.registerBean("second", WrapperFillHandler.class, () -> second);
            context.registerBean("custom", WrapperFillInterceptor.class, () -> custom);
            context.register(MyBatisPlusExConfig.class);
            context.refresh();
            assertEquals(1, context.getBeansOfType(WrapperFillInterceptor.class).size());
            assertSame(custom, context.getBean(WrapperFillInterceptor.class));
        }
    }

    @Test
    void multipleHandlerBeansStillProduceOneInterceptor() {
        try (AnnotationConfigApplicationContext context = context()) {
            context.registerBean("first", WrapperFillHandler.class, () -> fill -> { });
            context.registerBean("second", WrapperFillHandler.class, () -> fill -> { });
            context.register(MyBatisPlusExConfig.class);
            context.refresh();
            assertEquals(1, context.getBeansOfType(WrapperFillInterceptor.class).size());
        }
    }

    private AnnotationConfigApplicationContext context() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test",
                Collections.singletonMap("mybatis-plus-ex.entity-scan.enabled", false)));
        return context;
    }
}
