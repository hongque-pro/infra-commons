package com.labijie.infra.snowflake.config

import java.time.Duration

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-10
 */
class ZookeeperConfig(
    var server: String = "127.0.0.1:2181",
    var connectTimeout: Duration = Duration.ofSeconds(15),
    var sessionTimeout: Duration = Duration.ofHours(1)
)