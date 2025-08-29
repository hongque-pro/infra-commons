package com.labijie.infra.snowflake

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * @author Anders Xiao
 * @date 2025/8/29
 */
data class SnowflakeIdParts(
    val timestamp: Long,
    val dataCenterId: Long,
    val machineId: Long,
    val sequence: Long
) {
    fun timeGenerated(zoneId: ZoneId =  ZoneId.systemDefault()): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zoneId)
}


fun parseSnowflakeId(id: Long, startTimestamp: Long = SnowflakeProperties.DEFAULT_SNOWFLAKE_START): SnowflakeIdParts {
    val sequence = id and ((1L shl SnowflakeKernel.SEQUENCE_BIT) - 1)
    val machineId = (id shr SnowflakeKernel.MACHINE_LEFT) and ((1L shl SnowflakeKernel.MACHINE_BIT) - 1)
    val dataCenterId = (id shr SnowflakeKernel.DATACENTER_LEFT) and ((1L shl SnowflakeKernel.DATACENTER_BIT) - 1)
    val timestamp = (id shr SnowflakeKernel.TIMESTMP_LEFT) + startTimestamp
    return SnowflakeIdParts(timestamp, dataCenterId, machineId, sequence)
}