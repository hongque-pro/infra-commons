package com.labijie.infra.commons.snowflake.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-10
 */
@Configuration(proxyBeanMethods = false)
@ConfigurationProperties("infra.snowflake")
class SnowflakeProperties {
    var scope: String = "default"
    var startTimestamp: Long = 1480166465631L //2016-11-26 21:21:05
    var provider: String = "zookeeper"
    var zk: ZookeeperConfig = ZookeeperConfig()
    var static: StaticConfig = StaticConfig()
    var redis: RedisConfig = RedisConfig()
    var jdbc: JdbcConfig = JdbcConfig()
}