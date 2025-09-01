package com.labijie.infra

import com.labijie.infra.IIdGenerator

/**
 * @author Anders Xiao
 * @date 2025/9/1
 */
interface ITimeBasedIdGenerator : IIdGenerator {

    fun computeMaxId(
        utcEpochMilli: Long
    ): Long

    fun computeMinId(
        utcEpochMilli: Long
    ): Long

    fun computeEpochMilliFromId(id: Long): Long
}