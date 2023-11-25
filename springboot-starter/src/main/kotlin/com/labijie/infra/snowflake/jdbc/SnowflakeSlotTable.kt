package com.labijie.infra.snowflake.jdbc

import com.labijie.infra.orm.SimpleStringIdTable

/**
 *
 * @author: Anders Xiao
 * @Date: 2021/12/6
 * @Description:
 */


const val SnowflakeSlotTableName = "infra_snowflake_slots"

object SnowflakeSlotTable : SimpleStringIdTable(SnowflakeSlotTableName, "slot_number", 64) {

    val instance = varchar("instance", 64).index("instance_idx")
    val address = varchar("address", 64)
    val timeExpired = long("time_expired")

}