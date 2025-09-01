package com.labijie.infra.snowflake

import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.max

/**
 * Created with IntelliJ IDEA.
 *
 * @param nodeId Start from 1.
 * @param dataCenterId Start from 0.
 * @author Anders Xiao
 * @date 2018-08-10
 */
class SnowflakeKernel(
    nodeId: Long,
    private val dataCenterId: Long = 0,
    private val startTimestamp: Long = SnowflakeProperties.DEFAULT_SNOWFLAKE_START,
    private val bitsConfig: SnowflakeBitsConfig = SnowflakeBitsConfig()
) {
    private val machineId: Long = nodeId - 1L

    companion object {
        private val logger by lazy { LoggerFactory.getLogger(SnowflakeKernel::class.java) }
    }

    init {
        if (nodeId !in 1..(bitsConfig.maxMachineId + 1)) {
            throw SnowflakeException("Snowflake node id must be between 1 and ${(bitsConfig.maxMachineId + 1)}")
        }
        if (dataCenterId !in 0..bitsConfig.maxDatacenterId) {
            throw SnowflakeException("Snowflake data center id must be between 0 and ${bitsConfig.maxDatacenterId}")
        }

        val startDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimestamp), ZoneOffset.UTC)
        logger.info(
            """
    SnowflakeKernel initialized with:
        machineId       = $machineId (max: ${bitsConfig.maxMachineId})
        dataCenterId    = $dataCenterId (max: ${bitsConfig.maxDatacenterId})
        startTimestamp  = $startTimestamp (${startDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)})
        bitsConfig      = sequenceBits:${bitsConfig.sequenceBits} (IDs/ms: ${bitsConfig.maxSequence + 1}), 
                          machineBits:${bitsConfig.machineBits}, 
                          datacenterBits:${bitsConfig.datacenterBits}
    """.trimIndent()
        )
    }

    private val sequence = AtomicLong(0)
    private val lastTimestamp = AtomicLong(currentTime() - 1)

    // 上一次打印回拨日志的时间
    private val lastBackwardsLogTime = AtomicLong(0)

    private fun currentTime(): Long = System.currentTimeMillis()
    private fun waitNextMill(last: Long): Long = max(currentTime(), last + 1)

    fun computeMaxId(
        utcEpochMilli: Long,
    ): Long {
        val bitsConfig = bitsConfig
        val startTimestamp = startTimestamp
        val dataCenterId = dataCenterId
        val machineId = machineId

        // 时间差
        val timestampDiff = utcEpochMilli - startTimestamp
        require(timestampDiff >= 0) { "futureTimestamp must be after startTimestamp" }

        // 序列最大值
        val maxSeq = bitsConfig.maxSequence

        // 计算最大可能 ID
        return (timestampDiff shl bitsConfig.timestampShift) or
                (dataCenterId shl bitsConfig.datacenterShift) or
                (machineId shl bitsConfig.machineShift) or
                maxSeq
    }

    fun computeMinId(
        utcEpochMilli: Long,
    ): Long {
        val bitsConfig = bitsConfig
        val startTimestamp = startTimestamp
        val dataCenterId = dataCenterId
        val machineId = machineId

        // 时间差
        val timestampDiff = utcEpochMilli - startTimestamp
        require(timestampDiff >= 0) { "futureTimestamp must be after startTimestamp" }


        // 计算最大可能 ID
        return (timestampDiff shl bitsConfig.timestampShift) or
                (dataCenterId shl bitsConfig.datacenterShift) or
                (machineId shl bitsConfig.machineShift) or
                0
    }

    fun nextId(): Long {
        while (true) {
            val last = lastTimestamp.get()
            val now = currentTime()

            var curr = now
            // 时钟回拨处理
            if (curr < last) {
                curr = last + 1

                // 每分钟打印一次回拨警告日志
                val prevLog = lastBackwardsLogTime.get()
                if (now - prevLog >= 60_000) {
                    //避免多线程竞争，导致大量警告输出
                    if (lastBackwardsLogTime.compareAndSet(prevLog, now)) {
                        logger.warn(
                            "Clock moved backwards detected! \n" +
                            "lastTimestamp=$last, currentTime=$curr, machineId=$machineId, dataCenterId=$dataCenterId. \n" +
                            "Using +1 strategy to continue generating IDs."
                        )
                    }
                }
            }
            val seq = if (curr == last) {
                val s = (sequence.incrementAndGet() and bitsConfig.maxSequence)
                if (s == 0L) {
                    curr = waitNextMill(last)
                    sequence.set(0)
                    0L
                } else s
            } else {
                sequence.set(0)
                0L
            }

            if (lastTimestamp.compareAndSet(last, curr)) {
                return ((curr - startTimestamp) shl bitsConfig.timestampShift) or
                        (dataCenterId shl bitsConfig.datacenterShift) or
                        (machineId shl bitsConfig.machineShift) or
                        seq
            }
        }
    }
}