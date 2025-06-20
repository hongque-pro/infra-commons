package com.labijie.infra.snowflake

import com.labijie.infra.snowflake.config.EtcdConfig
import com.labijie.infra.snowflake.config.JdbcConfig
import com.labijie.infra.snowflake.config.RedisConfig
import com.labijie.infra.snowflake.config.StaticConfig
import com.labijie.infra.snowflake.config.ZookeeperConfig
import com.labijie.infra.utils.ifNullOrBlank
import org.slf4j.Logger
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.context.annotation.Configuration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * @author Anders Xiao
 * @date 2018-08-10
 */
@ConfigurationProperties("infra.snowflake")
class SnowflakeProperties {
    var scope: String = DEFAULT_SNOWFLAKE_SCOPE
    var startTimestamp: Long = DEFAULT_SNOWFLAKE_START //2016-11-26 21:21:05
    var provider: String = "static"


    @NestedConfigurationProperty
    val zk: ZookeeperConfig = ZookeeperConfig()

    @NestedConfigurationProperty
    val redis: RedisConfig = RedisConfig()

    @NestedConfigurationProperty
    val jdbc: JdbcConfig = JdbcConfig()

    @NestedConfigurationProperty
    val static: StaticConfig = StaticConfig()

    @NestedConfigurationProperty
    val etcd: EtcdConfig = EtcdConfig()


    fun fixedScope(): String {
        var scopeValue = scope
        if(scopeValue == DEFAULT_SNOWFLAKE_SCOPE) {
            scopeValue = System.getenv("SNOW_FLAKE_SCOPE").ifNullOrBlank { scope }
        }
        return scopeValue
    }

    companion object {
        const val DEFAULT_SNOWFLAKE_START =  1480166465631L
        const val DEFAULT_SNOWFLAKE_SCOPE =  "default"
    }
}
