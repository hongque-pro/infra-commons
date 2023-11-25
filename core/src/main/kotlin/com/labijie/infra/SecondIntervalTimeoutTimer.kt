package com.labijie.infra

import io.netty.util.HashedWheelTimer
import io.netty.util.Timeout
import io.netty.util.TimerTask
import com.labijie.infra.scheduling.CornTask
import com.labijie.infra.scheduling.DelayIntervalTask
import com.labijie.infra.scheduling.IntervalTask
import java.text.ParseException
import java.time.Duration
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-09-19
 */
object SecondIntervalTimeoutTimer {

    enum class TaskResult {
        Continue, Break
    }

    private val count = AtomicInteger(0)
    @Volatile
    private var started: Boolean = false


    private var timer = HashedWheelTimer({ r ->
        Thread(r,
                "Default-HashedWheelTimer-${this.count.incrementAndGet()}")
                .apply {
                    if (this.isDaemon) {
                        this.isDaemon = false
                    }
                    if (this.priority != Thread.NORM_PRIORITY) {
                        this.priority = Thread.NORM_PRIORITY
                    }
                }
    }, 500, TimeUnit.MILLISECONDS)

    @Deprecated("default property was deprecated, use instance method instead.")
    val default = timer

    val pendingTimeouts: Long
        get() = this.timer.pendingTimeouts()

    fun newTimeout(task: () -> Unit, delay: Long, unit: TimeUnit): Timeout {
        ensureStarted()
        return this.timer.newTimeout({ _ -> task.invoke() }, delay, unit)
    }

    fun newTimeout(task: TimerTask, delay: Long, unit: TimeUnit): Timeout {
        ensureStarted()
        return this.timer.newTimeout(task, delay, unit)
    }

    fun newTimeout(delayMillis: Long, task: () -> Unit): Timeout {
        return this.newTimeout(task, delayMillis, TimeUnit.MILLISECONDS)
    }

    fun interval(duration: Duration, task: (Timeout?) -> TaskResult): IntervalTask {
        if (duration.seconds < 1) {
            throw IllegalArgumentException("Task duration can not be less than 1 seconds.")
        }
        return IntervalTask.start(duration, task)
    }

    fun delayInterval(delay: Duration, interval: Duration, task: (Timeout?) -> TaskResult): DelayIntervalTask {
        if (delay.seconds < 1) {
            throw IllegalArgumentException("Task delay can not be less than 1 seconds.")
        }
        if (interval.seconds < 1) {
            throw IllegalArgumentException("Task interval can not be less than 1 seconds.")
        }
        return DelayIntervalTask.start(delay, interval, task)
    }


    @Throws(ParseException::class)
    fun corn(cornExpression: String, task: (Timeout?) -> TaskResult): CornTask {
        return CornTask.start(cornExpression, task)
    }

    private fun ensureStarted() {
        if (!started) {
            synchronized(this) {
                if (!started) {
                    this.timer.start()
                    Runtime.getRuntime().addShutdownHook(Thread {
                        this.timer.stop()
                    })
                    started = true
                }
            }
        }
    }
}