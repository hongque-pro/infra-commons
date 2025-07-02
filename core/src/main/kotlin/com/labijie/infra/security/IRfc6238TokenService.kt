package com.labijie.infra.security

import java.time.Duration

/**
 * @author Anders Xiao
 * @date 2025/7/2
 */
interface IRfc6238TokenService {
    fun generateCode(modifier: String? = null, timeStep: Duration? = null): Int

    fun validateCode(code: Int, modifier: String? = null, timeStep: Duration? = null): Boolean

    fun generateCodeString(modifier: String? = null, timeStep: Duration? = null): String

    fun validateCodeString(code: String, modifier: String? = null, timeStep: Duration? = null): Boolean
}