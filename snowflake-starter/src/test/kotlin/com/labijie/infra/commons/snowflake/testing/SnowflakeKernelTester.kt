package com.labijie.infra.commons.snowflake.testing

import com.labijie.infra.commons.snowflake.SnowflakeKernel
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentSkipListSet
import java.util.stream.IntStream

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-12
 */
class SnowflakeKernelTester{

    @Test
    fun testConnect(){
        for (i in 1..1024L) {
            SnowflakeKernel(i).nextId()
        }
        Assertions.assertThrows(IllegalArgumentException::class.java){
            SnowflakeKernel(0).nextId()
        }
        Assertions.assertThrows(IllegalArgumentException::class.java){
            SnowflakeKernel(1025).nextId()
        }
    }

    @Test
    fun testGeneration(){
        val k = SnowflakeKernel(1)
        val set = ConcurrentSkipListSet<Long>()
        val repeatCount = 1000
        IntStream.range(0, repeatCount).parallel().forEach{
            val id = k.nextId()
            if(!set.add(id)){
                println("id $id was exsited")
            }
        }
       Assertions.assertEquals(repeatCount, set.size)
    }
}