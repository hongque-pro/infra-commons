## 说明
该模块用于将日志输出到kafka中供后续下游系统使用。代码基于logback提供了kafka的log appender。
## 使用方法
### gradle依赖
```
compile "com.labijie.infra:x-infra-commons-logging:1.6.0"
```
### 应用日志
```xml
<!-- 备用的ConsoleAppender -->
<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
        <pattern>
            %d{yyyy-MM-dd HH:mm:ss.SSSXXX} [%level] [%thread] [%logger] - %m%n
        </pattern>
    </encoder>
</appender>

<appender name="KAFKA" class="com.labijie.infra.logging.appender.KafkaAppender">
    <encoder class="com.labijie.infra.logging.encoder.KafkaLayoutEncoder">
        <pattern>%d{yyyy-MM-dd'T'HH:mm:ss.SSSXXX} [%level] [%thread] [%logger] - %m%n</pattern>
    </encoder>
    <!-- 应用日志输出到kafka的topic名称，如果没有配置，将使用下面的projectName来决定 -->
    <topic>logs_match</topic>
    <!-- 业务日志输出到kafka的topic名称，如果没有配置，将使用下面的projectName来决定 -->
    <bizTopic>bizlogs_match</bizTopic>
    <!-- 应用名称，使用时根据实际项目修改。用于决定输出到kafka的topic名称，应用日志格式为logs_{projectName}，业务日志格式为bizlogs_{projectName} -->
    <projectName>match</projectName>
    <!-- 当向kafka集群写入失败时，最大重试次数-->
    <maxFailedTimes>5</maxFailedTimes>
    <!-- 当kafka不可用时，指定多长时间不往kafka写日志，而是写入到个备用appender里-->
    <attacheAppenderSecs>60</attacheAppenderSecs>
    <!-- kafka producer的配置，也可以通过环境变量的方式来指定，格式为类似LOGGING_MAX_BLOCK_MS=3000的，LOGGING_是前缀 -->
    <producerConfig>
        max.block.ms=3000
    </producerConfig>
    <!-- 备用appender -->
    <appender-ref ref="STDOUT"/>
    <!-- 通知下游系统对日志进行归档的方式，kafka里面不做处理。默认是monthly。 -->
    <archiveType>monthly</archiveType>
</appender>

<!-- 用于异步输出 -->
<appender name="ASYNC" class="com.labijie.infra.logging.appender.DiscardableAsyncAppender">
    <!-- 丢弃日志的级别，当queue达到discardingThreshold以后，指定级别以下的日志会被丢弃 -->
    <discardableLevel>INFO</discardableLevel>
    <!-- 默认是256，对于并发量较高的系统，队列深度需要根据业务场景进行相应的测试，做出相应的更改，以达到较好的性能。 -->
    <queueSize>10000</queueSize>
    <!-- 默认情况下，他的值为-1，表示queue的容量还有20%的时候 ，他将开始对日志进行丢弃 -->
    <discardingThreshold>0</discardingThreshold>
    <appender-ref ref="KAFKA"/>
</appender>
```

代码中使用BizLogger类来写入业务日志
```java
Map<String,?> data = new HashMap<>();
data.put("name", "张三");
data.put("age", 12);
BizLogger.log(BizEvent("test", data))
```
其中BizEvent类的结构说明如下:
- eventType 表示这次业务事件的类型
- eventId 表示这次业务事件的ID，下游系统可以使用该ID来做检索。比如ES中作为document的doc ID。
- data 表示这次业务事件的具体内容

### 例子
下面是一个完整的例子配置
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration debug="true">

    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>
                %d{yyyy-MM-dd HH:mm:ss.SSSXXX} [%level] [%thread] [%logger] - %m%n
            </pattern>
        </encoder>
    </appender>

    <appender name="KAFKA" class="com.labijie.infra.logging.appender.KafkaAppender">
        <encoder class="com.labijie.infra.logging.encoder.KafkaLayoutEncoder">
            <pattern>%d{yyyy-MM-dd'T'HH:mm:ss.SSSXXX} [%level] [%thread] [%logger] - %m%n</pattern>
        </encoder>
        <projectName>match</projectName>
        <maxFailedTimes>5</maxFailedTimes>
        <attacheAppenderSecs>60</attacheAppenderSecs>
        <producerConfig>
            max.block.ms=3000
        </producerConfig>
        <appender-ref ref="STDOUT"/>
    </appender>

    <appender name="ASYNC" class="com.labijie.infra.logging.appender.DiscardableAsyncAppender">
        <discardableLevel>INFO</discardableLevel>
        <appender-ref ref="KAFKA"/>
    </appender>
    
    <appender name="BIZ_KAFKA" class="com.labijie.infra.logging.appender.KafkaBizLogAppender">
        <projectName>match</projectName>
        <maxFailedTimes>5</maxFailedTimes>
        <attacheAppenderSecs>60</attacheAppenderSecs>
        <producerConfig>
            max.block.ms=3000
        </producerConfig>
        <appender-ref ref="STDOUT"/>
    </appender>

    <appender name="BIZ_ASYNC" class="com.labijie.infra.logging.appender.DiscardableAsyncAppender">
        <appender-ref ref="BIZ_KAFKA"/>
    </appender>

    <logger name="com.labijie.infra.logging.BizLogger" level="INFO" additivity="false">
        <appender-ref ref="BIZ_ASYNC"/>
    </logger>

    <logger name="com.labijie.infra" level="INFO"></logger>

    <root level="WARN">
        <appender-ref ref="ASYNC"/>
    </root>
</configuration>
```