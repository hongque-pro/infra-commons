package com.labijie.infra.commons.snowflake.configuration

import java.util.concurrent.TimeUnit

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-10
 */
class ZookeeperConfig(var server:String = "127.0.0.1:2181", var sessionTimeoutMs:Int = TimeUnit.HOURS.toMillis(1).toInt())