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

[![Star History Chart](https://api.star-history.com/svg?repos=taowater/mybatis-plus-ex&type=Date)](https://star-history.com/#taowater/mybatis-plus-ex&Date)

#### 定义Mapper，只用改包名

```java
import com.taowater.mpx.mapper.BaseMapper;

// 增强的BaseMapper，保留原BaseMapper所有方法，拓展功能

public interface PersonMapper extends BaseMapper<Person> {
}
```

#### 条件组装增强示例

```java
var list = personMapper.selectList(w -> w
        .geCol(Person::getFatherId, Person::getMotherId) // 字段间相比
        .eq(Person::getFamilyName, "刘") // 原有相等条件
        .eqX(Person::getName, "备") // 如果条件值为空(包括null和集合为空)，则不执行该行条件装配
        .eqR(Person::getDeathDate, null) // 如果条件值为空，则短路结果为空，不执行查询
        .limit(10) // limit n操作
```


        
