package com.labijie.infra.commons.snowflake.jdbc.pojo.dsl

import com.labijie.infra.commons.snowflake.jdbc.SnowflakeSlotTable
import com.labijie.infra.commons.snowflake.jdbc.SnowflakeSlotTable.address
import com.labijie.infra.commons.snowflake.jdbc.SnowflakeSlotTable.id
import com.labijie.infra.commons.snowflake.jdbc.SnowflakeSlotTable.instance
import com.labijie.infra.commons.snowflake.jdbc.SnowflakeSlotTable.timeExpired
import com.labijie.infra.commons.snowflake.jdbc.pojo.SnowflakeSlot
import kotlin.Boolean
import kotlin.Int
import kotlin.Number
import kotlin.String
import kotlin.Unit
import kotlin.collections.Iterable
import kotlin.collections.List
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.update

/**
 * DSL support for SnowflakeSlotTable
 *
 * This class made by a code generator (https://github.com/hongque-pro/infra-orm).
 *
 * Origin Exposed Table:
 * @see com.labijie.infra.commons.snowflake.jdbc.SnowflakeSlotTable
 */
public object SnowflakeSlotDSL {
  public fun parseSnowflakeSlotRow(raw: ResultRow): SnowflakeSlot {
    val plain = SnowflakeSlot()
    plain.instance = raw[instance]
    plain.address = raw[address]
    plain.timeExpired = raw[timeExpired]
    plain.id = raw[id]
    return plain
  }

  public fun applySnowflakeSlot(statement: UpdateBuilder<*>, raw: SnowflakeSlot): Unit {
    statement[instance] = raw.instance
    statement[address] = raw.address
    statement[timeExpired] = raw.timeExpired
    statement[id] = raw.id
  }

  public fun applySnowflakeSlot(statement: UpdateStatement, raw: SnowflakeSlot): Unit {
    statement[instance] = raw.instance
    statement[address] = raw.address
    statement[timeExpired] = raw.timeExpired
    statement[id] = raw.id
  }

  public fun ResultRow.toSnowflakeSlot(): SnowflakeSlot = parseSnowflakeSlotRow(this)

  public fun Iterable<ResultRow>.toSnowflakeSlotList(): List<SnowflakeSlot> =
      this.map(SnowflakeSlotDSL::parseSnowflakeSlotRow)

  public fun UpdateBuilder<*>.apply(raw: SnowflakeSlot) = applySnowflakeSlot(this, raw)

  public fun UpdateStatement.apply(raw: SnowflakeSlot) = applySnowflakeSlot(this, raw)

  public fun SnowflakeSlotTable.insert(raw: SnowflakeSlot): InsertStatement<Number> =
      SnowflakeSlotTable.insert {
    applySnowflakeSlot(it, raw)
  }

  public fun SnowflakeSlotTable.batchInsert(list: Iterable<SnowflakeSlot>): List<ResultRow> {
    val rows = SnowflakeSlotTable.batchInsert(list) {
      entry -> applySnowflakeSlot(this, entry)
    }
    return rows
  }

  public fun SnowflakeSlotTable.update(
    raw: SnowflakeSlot,
    limit: Int? = null,
    `where`: SqlExpressionBuilder.() -> Op<Boolean>
  ): Int = SnowflakeSlotTable.update(where, limit) {
    applySnowflakeSlot(it, raw)
  }

  public fun SnowflakeSlotTable.update(raw: SnowflakeSlot): Int = SnowflakeSlotTable.update(raw) {
    SnowflakeSlotTable.id eq id
  }

  public fun SnowflakeSlotTable.deleteByPrimaryKey(id: String): Int =
      SnowflakeSlotTable.deleteWhere {
    SnowflakeSlotTable.id eq id
  }

  public fun SnowflakeSlotTable.selectByPrimaryKey(id: String): SnowflakeSlot? {
    val query = SnowflakeSlotTable.select {
      SnowflakeSlotTable.id eq id
    }
    return query.firstOrNull()?.toSnowflakeSlot()
  }
}
