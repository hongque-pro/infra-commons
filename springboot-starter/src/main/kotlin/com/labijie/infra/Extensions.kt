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
    return applicationName.ifNullOrBlank("NULL_APP_NAME")
}

val Environment.isDevelopment
    get() = this.activeProfiles.any { Constants.DevelopmentProfile.equals(it, ignoreCase = true) || "development".equals(it, ignoreCase = true) }

val Environment.isProduction
    get() = this.activeProfiles.any { Constants.ProductionProfile.equals(it, ignoreCase = true) || "production".equals(it, ignoreCase = true) }

val Environment.isTest
    get() = this.activeProfiles.any { Constants.TestProfile.equals(it, ignoreCase = true) }

val Environment.isLocal
    get() = this.activeProfiles.any { Constants.LocalProfile.equals(it, ignoreCase = true) }

