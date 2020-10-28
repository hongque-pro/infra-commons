package com.labijie.infra.commons.snowflake.providers

import com.labijie.infra.commons.snowflake.ISlotProvider

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-06-19
 */
class StaticSlotProvider(private val slotId:Int = 1) : ISlotProvider {
    override fun acquireSlot(throwIfNoneSlot: Boolean): Int? {
        return slotId
    }
}