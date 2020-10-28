package com.labijie.infra.scheduling

import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.model.time.ExecutionTime
import com.cronutils.parser.CronParser
import io.netty.util.Timeout
import com.labijie.infra.SecondIntervalTimeoutTimer
import java.text.ParseException
import java.time.ZonedDateTime
import java.time.Instant


/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-29
 */
class CornTask private constructor(cornExpression: String, action: (Timeout?) -> SecondIntervalTimeoutTimer.TaskResult) : TimerTaskBase(action) {
    private val define = CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING)
    private val parser = CronParser(define)
    private var executionTime = ExecutionTime.forCron(parser.parse(cornExpression))

    override fun nextTimeoutMills(): Long {
        val now = ZonedDateTime.now()
        val time = executionTime.nextExecution(now)
        return if (time.isPresent) time.get().toInstant().toEpochMilli() else Instant.now().minusSeconds(10).toEpochMilli()
    }


    companion object {
        @JvmStatic
        @Throws(ParseException::class)
        fun start(cornExpression: String,
                  action: (Timeout?) -> SecondIntervalTimeoutTimer.TaskResult): CornTask {
            return CornTask(cornExpression, action).apply { this.run(null) }
        }
    }
}