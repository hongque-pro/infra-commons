package com.labijie.infra.scheduling

import io.netty.util.Timeout
import io.netty.util.TimerTask
import com.labijie.infra.SecondIntervalTimeoutTimer
import java.lang.IllegalArgumentException
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-29
 */
class IntervalTask private constructor(
        duration: Duration,
        action: (Timeout?) -> SecondIntervalTimeoutTimer.TaskResult) : TimerTaskBase(action) {

    private val duration = duration.toMillis()

    override fun nextTimeoutMills(): Long {
        return System.currentTimeMillis() +  duration
    }

    companion object {
        @JvmStatic
        fun start(duration: Duration,
                  action: (Timeout?) -> SecondIntervalTimeoutTimer.TaskResult): IntervalTask {
            return IntervalTask(duration, action).apply { this.run(null) }
        }
    }


    init {
        if (duration.seconds < 1) {
            throw IllegalArgumentException("Task duration can not be less than 1 seconds.")
        }
    }
}