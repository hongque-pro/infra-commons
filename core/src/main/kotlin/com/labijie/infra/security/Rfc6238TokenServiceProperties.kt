package com.labijie.infra.security

import java.time.Duration

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-07-14
 */
data class Rfc6238TokenServiceProperties(
        var timeStep: Duration = Duration.ofSeconds(60),
        var keyBase64:String = "T6F2zirG9CAmcqQJP9zl2OOZtudDDCitLbCOhlCXLuzhEm1Akp8Rp5V063zUAjLY+MFOQrDJ6o5Vv7x2yrxD1w==",
        var algorithm: Rfc6238Algorithm = Rfc6238Algorithm.HmacSHA1
)