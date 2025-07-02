package com.labijie.infra.testing

import com.labijie.infra.security.Rfc6238Algorithm
import com.labijie.infra.security.Rfc6238TokenService
import com.labijie.infra.security.Rfc6238TokenServiceProperties
import org.junit.jupiter.api.Assertions
import java.util.*
import kotlin.test.Test

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-07-14
 */
class Rfc6238TokenServiceTester {


    private fun rfc6238TokenService(alg: Rfc6238Algorithm): Rfc6238TokenService {
        val key = Rfc6238TokenService.randomKey()
        val properties = Rfc6238TokenServiceProperties(keyBase64 = key)
        return Rfc6238TokenService(properties)
    }

    @Test
    fun generateTest(){
        val svc = rfc6238TokenService(Rfc6238Algorithm.HmacSHA256)
        repeat(100){
            val modifier = UUID.randomUUID().toString()
            val code =  svc.generateCodeString(modifier)
            println(code)
            Assertions.assertTrue(code.length == 6)
            val valid = svc.validateCodeString(code, modifier)
            Assertions.assertTrue(valid)
        }
    }

    fun test(alg: Rfc6238Algorithm) {
        val svc = rfc6238TokenService(alg)

        val code = svc.generateCode()
        println(code)
        Assertions.assertEquals(true, svc.validateCode(code))


        val code2 = svc.generateCode()
        println(code2)
        Assertions.assertEquals(false, svc.validateCode(code2 + 2))

        val modifier = UUID.randomUUID().toString()
        val code3 = svc.generateCode(modifier)
        println(code3)
        Assertions.assertEquals(true, svc.validateCode(code3, modifier))
        Assertions.assertEquals(false, svc.validateCode(code3, modifier + "ccc"))
    }

    @Test
    fun testHmacSHA1() {
        test(Rfc6238Algorithm.HmacSHA1)
    }

    @Test
    fun testHmacSHA256() {
        test(Rfc6238Algorithm.HmacSHA256)
    }

    @Test
    fun testHmacSHA512() {
        test(Rfc6238Algorithm.HmacSHA512)
    }
}