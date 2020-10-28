package com.labijie.infra.distribution.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-09-19
 */
@ConfigurationProperties("infra.distribution.lock")
class DistributedLockConfig(var server:String = "127.0.0.1:2181") {
}