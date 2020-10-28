package com.labijie.infra.commons.snowflake.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-10
 */
@Configuration
@ConfigurationProperties("infra.snowflake")
class SnowflakeConfig {
    var scope:String = "default"
    var provider:String = "zookeeper"
    var zk:ZookeeperConfig = ZookeeperConfig()
    var static: StaticConfig = StaticConfig()
    var redis:RedisConfig = RedisConfig()
}