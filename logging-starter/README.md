## 说明
该模块在logging模块的基础上，作为springboot starter启动。额外提供了下面功能

- 将日志中的projectName与springboot项目的application name对比，看是否一致
- 提供业务日志的jdbc appender
 
## 使用方法
### gradle依赖
```
compile "com.labijie.infra:x-infra-commons-logging-starter:1.6.0"
```
### JDBC appender的配置方法
```xml
<!-- 备用的ConsoleAppender -->
<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
        <pattern>
            %d{yyyy-MM-dd HH:mm:ss.SSSXXX} [%level] [%thread] [%logger] - %m%n
        </pattern>
    </encoder>
</appender>

<appender name="BIZ_JDBC" class="com.labijie.infra.logging.appender.SpringbootJdbcBizLogAppender">
    <!-- 
    dataSourceConfigs中可配置多个业务事件类型和数据源的关系配置，每个关系之间用换行隔开，遵循properties格式
    格式为 eventType=table:dataSourceBeanName
    其中dataSourceBeanName如果未指定，将用默认的数据源来连接数据库，这个bean的名字为dataSource。
    如果项目中有多数据源的bean，需要指定bean名字
     -->
    <dataSourceConfigs>
        login=login_log
    </dataSourceConfigs>
    <appender-ref ref="STDOUT"/>
</appender>

<appender name="BIZ_ASYNC" class="com.labijie.infra.logging.appender.DiscardableAsyncAppender">
    <appender-ref ref="BIZ_JDBC"/>
</appender>

<logger name="com.labijie.infra.logging.BizLogger" level="INFO" additivity="false">
    <appender-ref ref="BIZ_ASYNC"/>
</logger>
```

代码中使用BizLogger类来写入业务日志
```java
Map<String,?> data = new HashMap<>();
data.put("name", "张三");
data.put("age", 12);
BizLogger.log(BizEvent("test","123", data))
```
其中BizEvent类的结构说明如下:
- eventType 表示这次业务事件的类型
- eventId 表示这次业务事件的ID，在JDBC appender中，将被作为数据库的主键id存储，类型为数字。
- data 表示这次业务事件的具体内容