package com.labijie.infra.testing

import com.labijie.infra.utils.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.RepeatedTest
import kotlin.random.Random
import kotlin.test.Test

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-14
 */
class ExtensionsTester {

    @Test
    fun networkAddress() {
        val address = findIpAddress("10.100.*.*")
        println("local address is $address")
    }

    @Test
    fun log() {
        println("test")
    }


    @Test
    fun longBytesTest() {
        repeat(10) {
            val v = Random.nextLong()
            val bytes = v.toByteArray()
            val v2 = bytes.toLong()
            Assertions.assertEquals(v, v2)
        }
    }

    @Test
    fun lntBytesTest() {
        repeat(10) {
            val v = Random.nextInt()
            val bytes = v.toByteArray()
            val v2 = bytes.toInt()
            Assertions.assertEquals(v, v2)
        }
    }
}