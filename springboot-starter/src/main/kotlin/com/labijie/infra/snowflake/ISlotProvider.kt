package com.labijie.infra.snowflake

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-12
 */

interface ISlotProvider {
    @Throws(SnowflakeException::class)
    fun acquireSlot(throwIfNoneSlot: Boolean = true) : Int?

    fun setMaxSlots(maxSlots: Int)
}
