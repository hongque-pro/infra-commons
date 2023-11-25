@file:Suppress("RedundantVisibilityModifier")

package com.labijie.infra.snowflake.jdbc.pojo.dsl

import com.labijie.infra.orm.OffsetList
import com.labijie.infra.orm.OffsetList.Companion.decodeToken
import com.labijie.infra.orm.OffsetList.Companion.encodeToken
import com.labijie.infra.snowflake.jdbc.SnowflakeSlotTable
import com.labijie.infra.snowflake.jdbc.SnowflakeSlotTable.address
import com.labijie.infra.snowflake.jdbc.SnowflakeSlotTable.id
import com.labijie.infra.snowflake.jdbc.SnowflakeSlotTable.instance
import com.labijie.infra.snowflake.jdbc.SnowflakeSlotTable.timeExpired
import com.labijie.infra.snowflake.jdbc.pojo.SnowflakeSlot
import java.lang.IllegalArgumentException
import java.util.Base64
import kotlin.Array
import kotlin.Boolean
import kotlin.Comparable
import kotlin.Int
import kotlin.Long
import kotlin.Number
import kotlin.String
import kotlin.Unit
import kotlin.collections.Collection
import kotlin.collections.Iterable
import kotlin.collections.List
import kotlin.collections.isNotEmpty
import kotlin.collections.last
import kotlin.collections.toList
import kotlin.reflect.KClass
import kotlin.text.Charsets
import kotlin.text.toByteArray
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.update

/**
 * DSL support for SnowflakeSlotTable
 *
 * This class made by a code generator (https://github.com/hongque-pro/infra-orm).
 *
 * Origin Exposed Table:
 * @see com.labijie.infra.springboot.snowflake.jdbc.SnowflakeSlotTable
 */
public object SnowflakeSlotDSL {
  public val SnowflakeSlotTable.allColumns: Array<Column<*>> by lazy {
    arrayOf(
    instance,
    address,
    timeExpired,
    id,
    )
  }


  public fun parseRow(raw: ResultRow): SnowflakeSlot {
    val plain = SnowflakeSlot()
    plain.instance = raw[instance]
    plain.address = raw[address]
    plain.timeExpired = raw[timeExpired]
    plain.id = raw[id]
    return plain
  }

  public fun parseRowSelective(row: ResultRow): SnowflakeSlot {
    val plain = SnowflakeSlot()
    if(row.hasValue(instance)) {
      plain.instance = row[instance]
    }
    if(row.hasValue(address)) {
      plain.address = row[address]
    }
    if(row.hasValue(timeExpired)) {
      plain.timeExpired = row[timeExpired]
    }
    if(row.hasValue(id)) {
      plain.id = row[id]
    }
    return plain
  }

  public fun <T> SnowflakeSlotTable.getColumnType(column: Column<T>): KClass<*> = when(column) {
    instance->String::class
    address->String::class
    timeExpired->Long::class
    id->String::class
    else->throw IllegalArgumentException("""Unknown column <${column.name}> for 'SnowflakeSlot'""")
  }

  @kotlin.Suppress("UNCHECKED_CAST")
  public fun <T> SnowflakeSlot.getColumnValue(column: Column<T>): T = when(column) {
    SnowflakeSlotTable.instance->this.instance as T
    SnowflakeSlotTable.address->this.address as T
    SnowflakeSlotTable.timeExpired->this.timeExpired as T
    SnowflakeSlotTable.id->this.id as T
    else->throw IllegalArgumentException("""Unknown column <${column.name}> for 'SnowflakeSlot'""")
  }

  public fun assign(
    builder: UpdateBuilder<*>,
    raw: SnowflakeSlot,
    selective: Array<out Column<*>>? = null,
    vararg ignore: Column<*>,
  ) {
    if((selective == null || selective.contains(instance)) && !ignore.contains(instance))
      builder[instance] = raw.instance
    if((selective == null || selective.contains(address)) && !ignore.contains(address))
      builder[address] = raw.address
    if((selective == null || selective.contains(timeExpired)) && !ignore.contains(timeExpired))
      builder[timeExpired] = raw.timeExpired
    if((selective == null || selective.contains(id)) && !ignore.contains(id))
      builder[id] = raw.id
  }

  public fun ResultRow.toSnowflakeSlot(vararg selective: Column<*>): SnowflakeSlot {
    if(selective.isNotEmpty()) {
      return parseRowSelective(this)
    }
    return parseRow(this)
  }

  public fun Iterable<ResultRow>.toSnowflakeSlotList(vararg selective: Column<*>):
      List<SnowflakeSlot> = this.map {
    it.toSnowflakeSlot(*selective)
  }

  public fun SnowflakeSlotTable.selectSlice(vararg selective: Column<*>): Query {
    val query = if(selective.isNotEmpty()) {
      SnowflakeSlotTable.slice(selective.toList()).selectAll()
    }
    else {
      SnowflakeSlotTable.selectAll()
    }
    return query
  }

  public fun UpdateBuilder<*>.setValue(raw: SnowflakeSlot, vararg ignore: Column<*>): Unit =
      assign(this, raw, ignore = ignore)

  public fun UpdateBuilder<*>.setValueSelective(raw: SnowflakeSlot, vararg selective: Column<*>):
      Unit = assign(this, raw, selective = selective)

  public fun SnowflakeSlotTable.insert(raw: SnowflakeSlot): InsertStatement<Number> =
      SnowflakeSlotTable.insert {
    assign(it, raw)
  }

  public fun SnowflakeSlotTable.batchInsert(
    list: Iterable<SnowflakeSlot>,
    ignoreErrors: Boolean = false,
    shouldReturnGeneratedValues: Boolean = false,
  ): List<ResultRow> {
    val rows = SnowflakeSlotTable.batchInsert(list, ignoreErrors, shouldReturnGeneratedValues) {
      entry -> assign(this, entry)
    }
    return rows
  }

  public fun SnowflakeSlotTable.update(
    raw: SnowflakeSlot,
    selective: Array<out Column<*>>? = null,
    ignore: Array<out Column<*>>? = null,
    limit: Int? = null,
    `where`: SqlExpressionBuilder.() -> Op<Boolean>,
  ): Int = SnowflakeSlotTable.update(`where`, limit) {
    val ignoreColumns = ignore ?: arrayOf()
    assign(it, raw, selective = selective, *ignoreColumns)
  }

  public fun SnowflakeSlotTable.updateByPrimaryKey(raw: SnowflakeSlot, vararg selective: Column<*>):
      Int = SnowflakeSlotTable.update(raw, selective = selective, ignore = arrayOf(id)) {
    SnowflakeSlotTable.id eq id
  }

  public fun SnowflakeSlotTable.deleteByPrimaryKey(id: String): Int =
      SnowflakeSlotTable.deleteWhere {
    SnowflakeSlotTable.id eq id
  }

  public fun SnowflakeSlotTable.selectByPrimaryKey(id: String, vararg selective: Column<*>):
      SnowflakeSlot? {
    val query = SnowflakeSlotTable.selectSlice(*selective).andWhere {
      SnowflakeSlotTable.id eq id
    }
    return query.firstOrNull()?.toSnowflakeSlot(*selective)
  }

  public fun SnowflakeSlotTable.selectByPrimaryKeys(ids: Iterable<String>, vararg
      selective: Column<*>): List<SnowflakeSlot> {
    val query = SnowflakeSlotTable.selectSlice(*selective).andWhere {
      SnowflakeSlotTable.id inList ids
    }
    return query.toSnowflakeSlotList(*selective)
  }

  public fun SnowflakeSlotTable.selectMany(vararg selective: Column<*>, `where`: Query.() -> Unit):
      List<SnowflakeSlot> {
    val query = SnowflakeSlotTable.selectSlice(*selective)
    `where`.invoke(query)
    return query.toSnowflakeSlotList(*selective)
  }

  public fun SnowflakeSlotTable.selectOne(vararg selective: Column<*>, `where`: Query.() -> Unit):
      SnowflakeSlot? {
    val query = SnowflakeSlotTable.selectSlice(*selective)
    `where`.invoke(query)
    return query.firstOrNull()?.toSnowflakeSlot(*selective)
  }

  public fun SnowflakeSlotTable.selectForwardByPrimaryKey(
    forwardToken: String? = null,
    order: SortOrder = SortOrder.DESC,
    pageSize: Int = 50,
    selective: Collection<Column<*>> = listOf(),
    `where`: (Query.() -> Unit)? = null,
  ): OffsetList<SnowflakeSlot> {
    if(pageSize < 1) {
      return OffsetList.empty()
    }
    val offsetKey = forwardToken?.let { Base64.getUrlDecoder().decode(it).toString(Charsets.UTF_8) }
    val query = SnowflakeSlotTable.selectSlice(*selective.toTypedArray())
    offsetKey?.let {
      when(order) {
        SortOrder.DESC, SortOrder.DESC_NULLS_FIRST, SortOrder.DESC_NULLS_LAST->
        query.andWhere { id less it }
        else-> query.andWhere { id greater it }
      }
    }
    `where`?.invoke(query)
    val sorted = query.orderBy(id, order)
    val list = sorted.limit(pageSize).toSnowflakeSlotList(*selective.toTypedArray())
    val token = if(list.size >= pageSize) {
      val lastId = list.last().id.toString().toByteArray(Charsets.UTF_8)
      Base64.getUrlEncoder().encodeToString(lastId)
    }
    else {
      null
    }
    return OffsetList(list, token)
  }

  public fun <T : Comparable<T>> SnowflakeSlotTable.selectForward(
    sortColumn: Column<T>,
    forwardToken: String? = null,
    order: SortOrder = SortOrder.DESC,
    pageSize: Int = 50,
    selective: Collection<Column<*>> = listOf(),
    `where`: (Query.() -> Unit)? = null,
  ): OffsetList<SnowflakeSlot> {
    if(pageSize < 1) {
      return OffsetList.empty()
    }
    if(sortColumn == id) {
      return this.selectForwardByPrimaryKey(forwardToken, order, pageSize, selective, `where`)
    }
    val kp = forwardToken?.let { decodeToken(it) }
    val offsetKey = kp?.first
    val excludeKeys = kp?.second
    val query = SnowflakeSlotTable.selectSlice(*selective.toTypedArray())
    offsetKey?.let {
      when(order) {
        SortOrder.DESC, SortOrder.DESC_NULLS_FIRST, SortOrder.DESC_NULLS_LAST->
        query.andWhere { sortColumn lessEq it }
        else-> query.andWhere { sortColumn greaterEq it }
      }
    }
    excludeKeys?.let {
      if(it.isNotEmpty()) {
        query.andWhere { id notInList it }
      }
    }
    `where`?.invoke(query)
    val sorted = query.orderBy(Pair(sortColumn, order), Pair(id, order))
    val list = sorted.limit(pageSize).toSnowflakeSlotList(*selective.toTypedArray())
    val token = if(list.size < pageSize) null else encodeToken(list, { getColumnValue(sortColumn) },
        SnowflakeSlot::id)
    return OffsetList(list, token)
  }
}
