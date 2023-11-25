@file:Suppress("RedundantVisibilityModifier")

package com.labijie.infra.snowflake.jdbc.pojo

import kotlin.Long
import kotlin.String

/**
 * POJO for SnowflakeSlotTable
 *
 * This class made by a code generator (https://github.com/hongque-pro/infra-orm).
 *
 * Origin Exposed Table:
 * @see com.labijie.infra.springboot.snowflake.jdbc.SnowflakeSlotTable
 */
public open class SnowflakeSlot {
  public var instance: String = ""

  public var address: String = ""

  public var timeExpired: Long = 0L

  public var id: String = ""
}
