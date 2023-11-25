package com.labijie.infra.snowflake

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-12
 */
class SnowflakeException(message: String? = null, cause: Throwable? = null, enableSuppression: Boolean = false, writableStackTrace: Boolean = true)
    : RuntimeException(message, cause, enableSuppression, writableStackTrace) {

    constructor(message:String) : this(message, null, false, true)
}