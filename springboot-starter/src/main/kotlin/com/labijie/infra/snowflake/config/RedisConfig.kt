package com.labijie.infra.snowflake.config

import java.time.Duration

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-09-09
 */
class RedisConfig(
    var url: String = "redis://localhost:6379",
    var sessionTimeout: Duration = Duration.ofHours(1)
)