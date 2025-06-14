package com.labijie.infra.distribution

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-09-19
 */
@ConfigurationProperties("infra.distribution")
class DistributedProperties() {
    var provider: String = "none"
    var zkServer:String = "127.0.0.1:2181"

    companion object {
        const val DISTRIBUTED_PROVIDER_ZOOKEEPER = "zookeeper"
    }
}