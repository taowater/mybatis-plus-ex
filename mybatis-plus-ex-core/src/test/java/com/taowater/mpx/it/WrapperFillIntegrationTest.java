package com.taowater.mpx.it;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.taowater.mpx.fill.WrapperFillHandler;
import com.taowater.mpx.interceptor.WrapperFillInterceptor;
import com.taowater.mpx.mapper.BaseMapper;
import com.taowater.mpx.method.ExMethodSqlInjector;
import com.taowater.mpx.wrapper.LambdaUpdateExWrapper;
import com.taowater.mpx.wrapper.UpdateExWrapper;
import com.taowater.mpx.wrapper.chain.LambdaUpdateChainExWrapper;
import com.taowater.mpx.wrapper.chain.UpdateChainExWrapper;
import lombok.Data;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class WrapperFillIntegrationTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 5, 12, 0);
    private static final LocalDateTime ENTITY_TIME = NOW.minusDays(1);
    private final AtomicInteger calls = new AtomicInteger();
    private SqlSessionFactory factory;
    private SqlSession session;
    private FillMapper mapper;

    @Data
    @TableName("fill_row")
    public static class FillRow {
        @TableId
        private Long id;
        private String name;
        @TableField("changed_by")
        private String modifier;
        @TableField(value = "changed_at", fill = FieldFill.UPDATE)
        private LocalDateTime updatedAt;
        private int counter;
        private String status = "DRAFT";
        @TableField(typeHandler = UppercaseHandler.class, jdbcType = JdbcType.VARCHAR)
        private String marker;
        private String nullableValue;
        @TableField(exist = false)
        private String unmapped;
        @TableLogic
        private Integer deleted;
    }

    @Data
    @TableName("plain_row")
    public static class PlainRow {
        @TableId
        private Long id;
        private String name;
    }

    public interface FillMapper extends BaseMapper<FillRow> {
    }

    public interface PlainMapper extends com.baomidou.mybatisplus.core.mapper.BaseMapper<PlainRow> {
    }

    public static class UppercaseHandler extends BaseTypeHandler<String> {
        @Override
        public void setNonNullParameter(PreparedStatement ps, int i, String value, JdbcType jdbcType) throws SQLException {
            ps.setString(i, value.toUpperCase(Locale.ROOT));
        }
        @Override
        public String getNullableResult(ResultSet rs, String column) throws SQLException {
            return rs.getString(column);
        }
        @Override
        public String getNullableResult(ResultSet rs, int column) throws SQLException {
            return rs.getString(column);
        }
        @Override
        public String getNullableResult(CallableStatement cs, int column) throws SQLException {
            return cs.getString(column);
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:wrapper_fill_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setEnvironment(new Environment("test", new JdbcTransactionFactory(), dataSource));
        configuration.setMapUnderscoreToCamelCase(true);
        GlobalConfig globalConfig = GlobalConfigUtils.defaults();
        globalConfig.setBanner(false);
        globalConfig.setSqlInjector(new ExMethodSqlInjector(com.baomidou.mybatisplus.annotation.DbType.H2));
        globalConfig.setMetaObjectHandler(new MetaObjectHandler() {
            @Override public void insertFill(MetaObject metaObject) { }
            @Override public void updateFill(MetaObject metaObject) {
                strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, ENTITY_TIME);
            }
        });
        GlobalConfigUtils.setGlobalConfig(configuration, globalConfig);
        WrapperFillHandler defaults = context -> context
                .fill("modifier", String.class, () -> "auto-" + calls.incrementAndGet())
                .fill("updatedAt", LocalDateTime.class, () -> NOW)
                .fill("marker", () -> "marker")
                .fill("nullableValue", () -> null)
                .fill("missing", () -> { throw new AssertionError("Missing property supplier was evaluated"); })
                .fill("unmapped", () -> { throw new AssertionError("Unmapped property supplier was evaluated"); })
                .fill("deleted", () -> { throw new AssertionError("Logic-delete property was filled"); });
        configuration.addInterceptor(new WrapperFillInterceptor(defaults));
        configuration.addMapper(FillMapper.class);
        configuration.addMapper(PlainMapper.class);
        factory = new MybatisSqlSessionFactoryBuilder().build(configuration);
        try (Connection c = dataSource.getConnection(); Statement st = c.createStatement()) {
            st.execute("CREATE TABLE fill_row(id BIGINT PRIMARY KEY,name VARCHAR(50),changed_by VARCHAR(50),"
                    + "changed_at TIMESTAMP,counter INT,status VARCHAR(20),marker VARCHAR(50),nullable_value VARCHAR(50),deleted INT)");
            st.execute("INSERT INTO fill_row VALUES(1,'old','old-user',NULL,7,'ACTIVE','old-marker','keep',0)");
            st.execute("CREATE TABLE plain_row(id BIGINT PRIMARY KEY,name VARCHAR(50))");
            st.execute("INSERT INTO plain_row VALUES(1,'old')");
        }
        session = factory.openSession(true);
        mapper = session.getMapper(FillMapper.class);
    }

    @AfterEach
    void tearDown() {
        session.close();
    }

    static Stream<Wrapper<FillRow>> wrappers() {
        return Stream.of(
                new UpdateWrapper<FillRow>().eq("id", 1L).set("name", "new"),
                new LambdaUpdateWrapper<FillRow>().eq(FillRow::getId, 1L).set(FillRow::getName, "new"),
                new UpdateExWrapper<FillRow>().eq("id", 1L).set("name", "new"),
                new LambdaUpdateExWrapper<FillRow>().eq(FillRow::getId, 1L).set(FillRow::getName, "new"));
    }

    @ParameterizedTest
    @MethodSource("wrappers")
    void fillsAllWrapperTypesWithoutCreatingEntityOrWritingBusinessDefaults(Wrapper<FillRow> wrapper) {
        assertThat(mapper.update(wrapper)).isEqualTo(1);
        FillRow row = mapper.selectById(1L);
        assertThat(row.getName()).isEqualTo("new");
        assertThat(row.getModifier()).isEqualTo("auto-1");
        assertThat(row.getUpdatedAt()).isEqualTo(NOW);
        assertThat(row.getMarker()).isEqualTo("MARKER");
        assertThat(row.getNullableValue()).isEqualTo("keep");
        assertThat(row.getCounter()).isEqualTo(7);
        assertThat(row.getStatus()).isEqualTo("ACTIVE");
        assertThat(wrapper.getEntity()).isNull();
    }

    @Test
    void explicitNullAndManualValueBothWinWithoutEvaluatingTheirSupplier() {
        mapper.update(new UpdateWrapper<FillRow>().eq("id", 1L).set("changed_by", null));
        assertThat(mapper.selectById(1L).getModifier()).isNull();
        mapper.update(new LambdaUpdateWrapper<FillRow>().eq(FillRow::getId, 1L).set(FillRow::getModifier, "manual"));
        assertThat(mapper.selectById(1L).getModifier()).isEqualTo("manual");
        assertThat(calls.get()).isZero();
    }

    @Test
    void conditionalFalseDoesNotCountAsAnAssignment() {
        mapper.update(new UpdateWrapper<FillRow>().eq("id", 1L).set("name", "new").set(false, "changed_by", null));
        assertThat(mapper.selectById(1L).getModifier()).isEqualTo("auto-1");
    }

    @Test
    void detectsQuotedColumnsAndMultipleRawAssignmentsWithNestedExpressions() {
        mapper.update(new UpdateWrapper<FillRow>().eq("id", 1L)
                .setSql("\"CHANGED_BY\"=COALESCE({0},'fallback,=x'), name=concat('a,b=',{1})", "manual", "tail"));
        FillRow row = mapper.selectById(1L);
        assertThat(row.getModifier()).isEqualTo("manual");
        assertThat(row.getName()).isEqualTo("a,b=tail");
        assertThat(calls.get()).isZero();
    }

    @Test
    void reusedWrapperKeepsItsOriginalAssignmentsAndGetsFreshDefaults() {
        UpdateWrapper<FillRow> wrapper = new UpdateWrapper<FillRow>().eq("id", 1L).set("name", "new");
        wrapper.getSqlSegment();
        String sqlSet = wrapper.getSqlSet();
        Map<String, Object> parameters = new LinkedHashMap<>(wrapper.getParamNameValuePairs());
        mapper.update(wrapper);
        mapper.update(wrapper);
        assertThat(mapper.selectById(1L).getModifier()).isEqualTo("auto-2");
        assertThat(wrapper.getSqlSet()).isEqualTo(sqlSet);
        assertThat(wrapper.getParamNameValuePairs()).isEqualTo(parameters);
    }

    @Test
    void entityAssignmentsAndExistingMetaObjectHandlerContinueToWin() {
        FillRow entity = new FillRow();
        entity.setModifier("from-entity");
        mapper.update(entity, new UpdateWrapper<FillRow>().eq("id", 1L).set("name", "new"));
        FillRow row = mapper.selectById(1L);
        assertThat(row.getModifier()).isEqualTo("from-entity");
        assertThat(row.getUpdatedAt()).isEqualTo(ENTITY_TIME);
        assertThat(calls.get()).isZero();
    }

    @Test
    void oneGlobalHandlerSkipsEntitiesWithoutThoseFields() {
        PlainMapper plain = session.getMapper(PlainMapper.class);
        plain.update(new UpdateWrapper<PlainRow>().eq("id", 1L).set("name", "new"));
        assertThat(plain.selectById(1L).getName()).isEqualTo("new");
        assertThat(calls.get()).isZero();
    }

    @Test
    void consumerAndBothChainEntrypointsUseTheSameDefaults() {
        mapper.update(w -> w.eq(FillRow::getId, 1L).set(FillRow::getName, "consumer"));
        assertThat(new UpdateChainExWrapper<>(mapper).eq("id", 1L).set("name", "chain").update()).isTrue();
        assertThat(new LambdaUpdateChainExWrapper<>(mapper)
                .eq(FillRow::getId, 1L).set(FillRow::getName, "lambda-chain").update()).isTrue();
        assertThat(mapper.selectById(1L).getModifier()).isEqualTo("auto-3");
    }

    @Test
    void defaultsWorkWithBatchExecution() {
        try (SqlSession batch = factory.openSession(ExecutorType.BATCH, false)) {
            FillMapper batchMapper = batch.getMapper(FillMapper.class);
            batchMapper.update(new UpdateWrapper<FillRow>().eq("id", 1L).set("name", "first"));
            batchMapper.update(new UpdateWrapper<FillRow>().eq("id", 1L).set("name", "second"));
            batch.commit();
        }
        assertThat(mapper.selectById(1L).getModifier()).isEqualTo("auto-2");
    }

    @Test
    void defaultsCanSupplyTheEntireSetAndDoNotAffectInsertOrDelete() {
        mapper.update(new UpdateWrapper<FillRow>().eq("id", 1L));
        assertThat(mapper.selectById(1L).getModifier()).isEqualTo("auto-1");
        PlainMapper plain = session.getMapper(PlainMapper.class);
        PlainRow added = new PlainRow();
        added.setId(2L);
        added.setName("added");
        plain.insert(added);
        plain.deleteById(2L);
        assertThat(calls.get()).isEqualTo(1);
    }

    @Test
    void updateByIdKeepsNativeEntityFillWithoutApplyingWrapperDefaults() {
        FillRow entity = new FillRow();
        entity.setId(1L);
        entity.setName("by-id");
        mapper.updateById(entity);
        FillRow row = mapper.selectById(1L);
        assertThat(row.getUpdatedAt()).isEqualTo(ENTITY_TIME);
        assertThat(row.getModifier()).isEqualTo("old-user");
        assertThat(row.getMarker()).isEqualTo("old-marker");
        assertThat(calls.get()).isZero();
    }
}
