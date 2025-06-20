package com.labijie.infra.snowflake.config

import com.labijie.infra.snowflake.HostIdentifier
import java.time.Duration

/**
 * @author Anders Xiao
 * @date 2025/6/20
 */
class EtcdConfig {
    var endpoints: String = "http://localhost:2379"
    var ttl: Duration = Duration.ofMinutes(3)
    var connectTimeout: Duration = Duration.ofMinutes(30)
    var identifier: HostIdentifier = HostIdentifier.HostName
}