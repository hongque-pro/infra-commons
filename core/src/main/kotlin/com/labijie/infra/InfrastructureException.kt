package com.labijie.infra

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-12
 */
open class InfrastructureException(message: String? = null, cause: Throwable? = null, enableSuppression: Boolean = false, writableStackTrace: Boolean = true)
    : RuntimeException(message, cause, enableSuppression, writableStackTrace) {

    constructor(message:String) : this(message, null, false, true)
}

class NullApplicationNameException() : InfrastructureException("Spring application name must be configured (spring.application.name).")