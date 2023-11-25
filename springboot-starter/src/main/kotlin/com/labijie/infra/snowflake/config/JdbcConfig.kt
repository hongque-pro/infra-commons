package com.labijie.infra.snowflake.config

import java.time.Duration
/**
 *
 * @Auther: AndersXiao
 * @Date: 2021/12/6
 * @Description:
 */
class JdbcConfig {
    var instanceIdentity: InstanceIdentity = InstanceIdentity.UUID
    var timeout: Duration = Duration.ofHours(1)
}

enum class InstanceIdentity {
    IP,
    UUID
}