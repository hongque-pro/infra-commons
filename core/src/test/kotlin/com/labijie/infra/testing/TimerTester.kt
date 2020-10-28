package com.labijie.infra.testing

import com.labijie.infra.SecondIntervalTimeoutTimer
import com.labijie.infra.utils.nowString
import org.junit.jupiter.api.Assertions
import kotlin.test.Test
import java.time.Duration
import kotlin.random.Random
import kotlin.test.BeforeTest

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-02-19
 */
class TimerTester {

    private var count = 1

    @BeforeTest
    fun init() {
        count = 0
    }

    @Test
    fun interval() {
        SecondIntervalTimeoutTimer.interval(Duration.ofSeconds(1)) {
            count++
            println("invoked")
            if (count == 3) {
                println("canceled")
                SecondIntervalTimeoutTimer.TaskResult.Break
            } else {
                SecondIntervalTimeoutTimer.TaskResult.Continue
            }
        }
        Thread.sleep(5000)
        Assertions.assertEquals(3, count)
    }

    @Test
    fun delayInterval() {
        println("start $count:  ${nowString()}")
        SecondIntervalTimeoutTimer.delayInterval(Duration.ofSeconds(3), Duration.ofSeconds(1)) {
            count++
            println("invoked $count:  ${nowString()}")
            if (count == 100) {
                println("canceled")
                SecondIntervalTimeoutTimer.TaskResult.Break
            } else {
                SecondIntervalTimeoutTimer.TaskResult.Continue
            }
        }
        Thread.sleep(10000)
        Assertions.assertTrue(count <= 10)
    }

    @Test
    fun multiThreads() {
        println("start $count:  ${nowString()}")
        repeat(10) {
            SecondIntervalTimeoutTimer.interval(Duration.ofSeconds(Random.nextLong(1, 3))) {
                count++
                println("[${Thread.currentThread().name}] invoked $count:  ${nowString()},  thread: ${Thread.currentThread().id}")
                SecondIntervalTimeoutTimer.TaskResult.Continue
            }
        }
        Thread.sleep(10000)

    }
}