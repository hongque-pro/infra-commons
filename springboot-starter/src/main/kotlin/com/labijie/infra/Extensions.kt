package com.labijie.infra

import com.labijie.infra.utils.Constants
import com.labijie.infra.utils.ifNullOrBlank
import org.springframework.core.env.Environment

/**
 * @author Anders Xiao
 * @date 2018-09-19
 */
fun Environment.getApplicationName(throwIfNotConfigured:Boolean = false):String {
    val applicationName: String? = this.getProperty("spring.application.name")
    if(applicationName.isNullOrBlank() && throwIfNotConfigured) {
        throw NullApplicationNameException()
    }
    return applicationName.ifNullOrBlank("NULL_APP_NAME")!!
}

val Environment.isDevelopment
    get() = this.activeProfiles.contains(Constants.LocalProfile) || this.activeProfiles.contains(Constants.DevelopmentProfile)

val Environment.isProduction
    get() = this.activeProfiles.contains(Constants.ProductionProfile)

val Environment.IsTest
    get() = this.activeProfiles.contains(Constants.TestProfile)

