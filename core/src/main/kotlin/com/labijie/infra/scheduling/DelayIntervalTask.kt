package com.labijie.infra.scheduling

import io.netty.util.Timeout
import com.labijie.infra.SecondIntervalTimeoutTimer
import java.time.Duration

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-29
 */
class DelayIntervalTask private constructor(
        delay:Duration,
        interval:Duration,
        action: (Timeout?) -> SecondIntervalTimeoutTimer.TaskResult): TimerTaskBase(action) {

    private var first = true
    private val delay = delay.toMillis()
    private val interval = interval.toMillis()

    companion object {
        @JvmStatic
        fun start(delay:Duration,
                  interval:Duration,
                  action: (Timeout?) -> SecondIntervalTimeoutTimer.TaskResult): DelayIntervalTask {
            return DelayIntervalTask(delay, interval, action).apply { this.run(null) }
        }
    }

    override fun nextTimeoutMills(): Long {
        val next = if(first){
            first = false
            this.delay
        }else{
            interval
        }
        return System.currentTimeMillis() + next
    }


}