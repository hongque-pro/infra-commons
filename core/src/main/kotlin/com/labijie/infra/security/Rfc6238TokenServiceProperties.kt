package com.labijie.infra.security

import java.time.Duration

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-07-14
 */
data class Rfc6238TokenServiceProperties(
        var timeStep: Duration = Duration.ofMinutes(3),
        var securityToken:String = "QWERasdfVCXZ"
)