package com.labijie.infra.snowflake

import com.labijie.infra.snowflake.config.JdbcConfig
import com.labijie.infra.snowflake.config.RedisConfig
import com.labijie.infra.snowflake.config.StaticConfig
import com.labijie.infra.snowflake.config.ZookeeperConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.context.annotation.Configuration

/**
 * @author Anders Xiao
 * @date 2018-08-10
 */
@Configuration(proxyBeanMethods = false)
@ConfigurationProperties("infra.snowflake")
class SnowflakeProperties {
    var scope: String = "default"
    var startTimestamp: Long = 1480166465631L //2016-11-26 21:21:05
    var provider: String = "static"


    @NestedConfigurationProperty
    val zk: ZookeeperConfig = ZookeeperConfig()

    @NestedConfigurationProperty
    val redis: RedisConfig = RedisConfig()

    @NestedConfigurationProperty
    val jdbc: JdbcConfig = JdbcConfig()

    @NestedConfigurationProperty
    val static: StaticConfig = StaticConfig()
}
