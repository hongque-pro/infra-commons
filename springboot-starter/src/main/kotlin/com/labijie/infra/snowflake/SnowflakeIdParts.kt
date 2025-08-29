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
    fun timeGenerated(zoneId: ZoneId = ZoneId.systemDefault()): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zoneId)
}


fun parseSnowflakeId(
    id: Long,
    startTimestamp: Long = SnowflakeProperties.DEFAULT_SNOWFLAKE_START,
    bitsConfig: SnowflakeBitsConfig = SnowflakeBitsConfig(),
): SnowflakeIdParts {
    val sequence = id and ((1L shl bitsConfig.sequenceBits) - 1)
    val machineId = (id shr bitsConfig.machineShift) and ((1L shl bitsConfig.machineBits) - 1)
    val dataCenterId = (id shr bitsConfig.datacenterShift) and ((1L shl bitsConfig.datacenterBits) - 1)
    val timestamp = (id shr bitsConfig.timestampShift) + startTimestamp
    return SnowflakeIdParts(timestamp, dataCenterId, machineId, sequence)
}