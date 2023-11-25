package com.labijie.infra.snowflake

import com.labijie.infra.json.JacksonHelper
import java.util.*

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-13
 */
data class WorkNodeInfo(var ipAddress: String = "",
                   var applicationName: String = "",
                   var instanceStamp:String = UUID.randomUUID().toString()) {
    companion object {
        fun fromBytes(byteArray: ByteArray): WorkNodeInfo {
            return JacksonHelper.deserialize(byteArray, WorkNodeInfo::class)
        }
    }

    fun toBytes(): ByteArray {
        return JacksonHelper.serialize(this)
    }
}