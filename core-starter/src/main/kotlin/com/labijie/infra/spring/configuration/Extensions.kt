package com.labijie.infra.spring.configuration

import com.labijie.infra.utils.Constants
import com.labijie.infra.utils.ifNullOrBlank
import org.springframework.core.env.Environment

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-09-19
 */
fun Environment.getApplicationName(throwIfNotConfigured:Boolean = true):String {
    val applicationName: String? = this.getProperty("spring.application.name")
    if(applicationName.isNullOrBlank() && throwIfNotConfigured) {
        throw IllegalArgumentException("Spring application name must be configured.")
    }
    return applicationName.ifNullOrBlank("NULL_APP_NAME")!!
}

val Environment.isDevelopment
get() = this.activeProfiles.contains(Constants.LocalProfile) || this.activeProfiles.contains(Constants.DevelopmentProfile)