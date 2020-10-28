package com.labijie.infra.scheduling

import io.netty.util.Timeout
import io.netty.util.TimerTask
import com.labijie.infra.NamedThreadFactory
import com.labijie.infra.SecondIntervalTimeoutTimer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-29
 */
abstract class TimerTaskBase(private val action: (Timeout?) -> SecondIntervalTimeoutTimer.TaskResult) : TimerTask {

    companion object {
        @JvmStatic
        private val executor: ExecutorService = Executors.newCachedThreadPool(NamedThreadFactory(false) { "timer-task-#$it" }).apply {
            Runtime.getRuntime().addShutdownHook(Thread {
                this.shutdown()
            })
        }
    }

    @Volatile
    private var isStarted = false

    var isCancelled: Boolean = false
        private set

    val isRunning: Boolean
        get() = isStarted

    var invocationCount: Int = 0

    override fun run(timeout: Timeout?) {
        try {
            executor.submit {
                var result = SecondIntervalTimeoutTimer.TaskResult.Continue
                try {
                    if (isStarted) {
                        result = if (!isCancelled) {
                            invocationCount++
                            action.invoke(timeout)
                        } else {
                            SecondIntervalTimeoutTimer.TaskResult.Break
                        }
                    } else {
                        isStarted = true
                    }

                } finally {
                    if (result == SecondIntervalTimeoutTimer.TaskResult.Continue) {
                        val mills = this.nextTimeoutMills() - System.currentTimeMillis()
                        if (mills >= 1000) {
                            SecondIntervalTimeoutTimer.newTimeout(this, mills, TimeUnit.MILLISECONDS)
                        } else {
                            isStarted = false
                        }
                    } else {
                        isStarted = false
                    }
                }
            }
        }catch (ex: RejectedExecutionException){

        }
    }

    fun cancel() {
        this.isCancelled = true
    }

    abstract fun nextTimeoutMills(): Long
}