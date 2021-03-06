package com.labijie.infra.commons.snowflake.configuration

import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-09-09
 */
class RedisConfig(
        var url:String = "redis://localhost:6379",
        var sessionTimeout:Duration = Duration.ofHours(1)) {
}