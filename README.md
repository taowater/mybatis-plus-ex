# Mybatis-Plus-Ex

<p align="center">
	<a target="_blank" href="https://central.sonatype.com/artifact/io.github.taowater/mybatis-plus-ex">
		<img src="https://img.shields.io/maven-central/v/io.github.taowater/mybatis-plus-ex?label=Maven%20Central" />
	</a>
	<a target="_blank" href="https://github.com/taowater/mybatis-plus-ex/blob/main/LICENSE">
		<img src="https://img.shields.io/github/license/taowater/mybatis-plus-ex.svg" />
	</a>
	<a target="_blank" href="https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html">
		<img src="https://img.shields.io/badge/JDK-8+-green.svg" />
	</a>
	<a target="_blank" href='https://github.com/taowater/mybatis-plus-ex'>
		<img src="https://img.shields.io/github/stars/taowater/mybatis-plus-ex.svg?style=social" alt="github star"/>
	</a>
</p>

对Mybatis-Plus用法上的一些拓展

### 🍊Maven

```xml

<dependency>
    <groupId>io.github.taowater</groupId>
    <artifactId>mybatis-plus-ex</artifactId>
    <version>LATEST</version>
</dependency>
```

### 安全提示

字符串列名条件（非 Lambda）默认**不**开启 SQL 注入校验。对不可信输入拼入列名时，请显式调用：

```java
new QueryExWrapper<>(Entity.class).checkSqlInjection().eq("user_name", name);
```

Lambda 列名由实体元数据解析，一般无需额外校验。

[![Star History Chart](https://api.star-history.com/svg?repos=taowater/mybatis-plus-ex&type=Date)](https://star-history.com/#taowater/mybatis-plus-ex&Date)

#### 定义Mapper，只用改包名

```java
import com.taowater.mpx.mapper.BaseMapper;

// 增强的BaseMapper，保留原BaseMapper所有方法，拓展功能

public interface PersonMapper extends BaseMapper<Person> {
}
```

#### Wrapper 更新默认填充

使用 Boot Starter 时，声明一次 `WrapperFillHandler` Bean，所有使用本库 `ExMethodSqlInjector`
注入的 `update(entity, wrapper)` 都会应用这些规则。原生和扩展的字符串/Lambda 更新 Wrapper、
Consumer 更新及链式更新均支持，不需要创建空实体，也不要求字段配置 `FieldFill` 注解。

```java
import com.taowater.mpx.fill.WrapperFillHandler;

@Bean
public WrapperFillHandler wrapperFillHandler() {
    return context -> context
        .fill("updateTime", LocalDateTime.class, LocalDateTime::now)
        .fill("updateUser", Long.class, () -> currentUserId());
}
```

`currentUserId()` 由业务提供；属性名和规则也由业务自由定义，框架不内置审计字段。
配置使用 **Java 属性名**，实际 SQL 列名、JDBC 类型和 TypeHandler 从实体映射读取。
可省略类型参数，例如 `.fill("operator", () -> currentOperator())`。

- 当前实体没有对应映射属性（包括 `exist = false`）时跳过，不调用 Supplier。
- 手动 `set`、`setSql`、自增/自减表达式已指定的列优先，包括显式 `set(column, null)`。
- `set(false, ...)` 不算已赋值；实体参数中已参与 `SET` 的属性也不会重复填充。
- Supplier 返回 `null` 时不追加更新。多个 Handler 按 Spring 顺序执行，先成功填充者优先。
- 不修改原 Wrapper；重复执行时重新计算默认值，不会积累重复赋值。
- 仅处理普通映射属性，不自动填写主键或逻辑删除列。

非 Spring 环境在 MyBatis Configuration 中注册一次：

```java
configuration.addInterceptor(new WrapperFillInterceptor(wrapperFillHandler));
```

需同时使用本库 `ExMethodSqlInjector`。该功能针对它注入的 `update`，不改写手工 XML/注解 SQL、
`updateById`、插入或删除。传实体的更新仍使用原有 `MetaObjectHandler`；Wrapper 默认填充不会替代它。
Wrapper 单参数/Consumer 更新已取消创建空实体，应改用上述 Handler 配置默认值。

`setSql` 支持常见的列赋值、引号/限定列名、多个赋值及嵌套表达式；若无法可靠识别赋值目标，
会明确报错，不冒险覆盖手动赋值。复杂方言语法可改用明确的 `set`。

#### BaseMapper 注入诊断

使用 `mybatis-plus-ex-spring` 的实体扫描配置（Boot Starter 默认开启）时，框架会自动增强
`@Autowired BaseMapper<T>` 的缺失 Bean 提示。实体未被扫描或动态 Mapper 生成失败时，启动异常会包含
具体实体、注入字段和宿主 Service，例如：

```text
No generated BaseMapper bean found for entity com.example.User ...
Required by ...BaseAdminServiceImpl.baseMapper ...
```

因此无需为获得实体信息而修改业务 Service 的注入写法；`@Lazy` 注入在首次调用失败时也会输出相同的实体诊断信息。

#### 条件组装增强示例

```java
var list = personMapper.selectList(w -> w
        .geCol(Person::getFatherId, Person::getMotherId) // 字段间相比
        .eq(Person::getFamilyName, "刘") // 原有相等条件
        .eqX(Person::getName, "备") // 如果条件值为空(包括null和集合为空)，则不执行该行条件装配
        .eqR(Person::getDeathDate, null) // 如果条件值为空，则短路结果为空，不执行查询
        .limit(10) // limit n操作
```


        
