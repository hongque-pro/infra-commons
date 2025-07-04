@file:Suppress("RedundantVisibilityModifier")

package com.labijie.infra.snowflake.jdbc.pojo.dsl

import com.labijie.infra.orm.OffsetList
import com.labijie.infra.snowflake.jdbc.SnowflakeSlotTable
import com.labijie.infra.snowflake.jdbc.SnowflakeSlotTable.address
import com.labijie.infra.snowflake.jdbc.SnowflakeSlotTable.id
import com.labijie.infra.snowflake.jdbc.SnowflakeSlotTable.instance
import com.labijie.infra.snowflake.jdbc.SnowflakeSlotTable.timeExpired
import com.labijie.infra.snowflake.jdbc.pojo.SnowflakeSlot
import java.lang.IllegalArgumentException
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
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.replace
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.ReplaceStatement
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.statements.UpsertBuilder
import org.jetbrains.exposed.sql.statements.UpsertStatement
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert

/**
 * DSL support for SnowflakeSlotTable
 *
 * This code generated by an open-source project: Infra-Orm 
 * Project Site: https://github.com/hongque-pro/infra-orm.
 *
 * Generator Version: 2.1.0
 *
 *
 * Don't modify these codes !!
 *
 * Origin Exposed Table:
 * @see com.labijie.infra.snowflake.jdbc.SnowflakeSlotTable
 */
@kotlin.Suppress(
  "unused",
  "DuplicatedCode",
  "MemberVisibilityCanBePrivate",
  "RemoveRedundantQualifierName",
)
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

  private fun <T> SnowflakeSlot.getColumnValueString(column: Column<T>): String = when(column) {
    SnowflakeSlotTable.instance->this.instance
    SnowflakeSlotTable.address->this.address
    SnowflakeSlotTable.timeExpired -> this.timeExpired.toString()

    SnowflakeSlotTable.id->this.id
    else->throw IllegalArgumentException("""Can't converter value of SnowflakeSlot::${column.name} to string.""")
  }

  @kotlin.Suppress("UNCHECKED_CAST")
  private fun <T> parseColumnValue(valueString: String, column: Column<T>): T {
    val value = when(column) {
      SnowflakeSlotTable.instance -> valueString
      SnowflakeSlotTable.address -> valueString
      SnowflakeSlotTable.timeExpired ->valueString.toLong()
      SnowflakeSlotTable.id -> valueString
      else->throw IllegalArgumentException("""Can't converter value of SnowflakeSlot::${column.name} to string.""")
    }
    return value as T
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
    val list = if(selective.isNullOrEmpty()) null else selective
    if((list == null || list.contains(instance)) && !ignore.contains(instance))
      builder[instance] = raw.instance
    if((list == null || list.contains(address)) && !ignore.contains(address))
      builder[address] = raw.address
    if((list == null || list.contains(timeExpired)) && !ignore.contains(timeExpired))
      builder[timeExpired] = raw.timeExpired
    if((list == null || list.contains(id)) && !ignore.contains(id))
      builder[id] = raw.id
  }

  public fun ResultRow.toSnowflakeSlot(vararg selective: Column<*>): SnowflakeSlot {
    if(selective.isNotEmpty()) {
      return parseRowSelective(this)
    }
    return parseRow(this)
  }

  public fun Iterable<ResultRow>.toSnowflakeSlotList(vararg selective: Column<*>): List<SnowflakeSlot> = this.map {
    it.toSnowflakeSlot(*selective)
  }

  public fun SnowflakeSlotTable.selectSlice(vararg selective: Column<*>): Query {
    val query = if(selective.isNotEmpty()) {
      select(selective.toList())
    }
    else {
      selectAll()
    }
    return query
  }

  public fun UpdateBuilder<*>.setValue(raw: SnowflakeSlot, vararg ignore: Column<*>): Unit = assign(this, raw, ignore = ignore)

  public fun UpdateBuilder<*>.setValueSelective(raw: SnowflakeSlot, vararg selective: Column<*>): Unit = assign(this, raw, selective = selective)

  public fun SnowflakeSlotTable.insert(raw: SnowflakeSlot): InsertStatement<Number> = insert {
    assign(it, raw)
  }

  public fun SnowflakeSlotTable.insertIgnore(raw: SnowflakeSlot): InsertStatement<Long> = insertIgnore {
    assign(it, raw)
  }

  public fun SnowflakeSlotTable.upsert(
    raw: SnowflakeSlot,
    onUpdateExclude: List<Column<*>>? = null,
    onUpdate: (UpsertBuilder.(UpdateStatement) -> Unit)? = null,
    `where`: (SqlExpressionBuilder.() -> Op<Boolean>)? = null,
  ): UpsertStatement<Long> = upsert(where = where, onUpdate = onUpdate, onUpdateExclude = onUpdateExclude) {
    assign(it, raw)
  }

  public fun SnowflakeSlotTable.batchInsert(
    list: Iterable<SnowflakeSlot>,
    ignoreErrors: Boolean = false,
    shouldReturnGeneratedValues: Boolean = false,
  ): List<ResultRow> {
    val rows = batchInsert(list, ignoreErrors, shouldReturnGeneratedValues) {
      entry -> assign(this, entry)
    }
    return rows
  }

  public fun SnowflakeSlotTable.batchUpsert(
    list: Iterable<SnowflakeSlot>,
    onUpdateExclude: List<Column<*>>? = null,
    onUpdate: (UpsertBuilder.(UpdateStatement) -> Unit)? = null,
    shouldReturnGeneratedValues: Boolean = false,
    `where`: (SqlExpressionBuilder.() -> Op<Boolean>)? = null,
  ): List<ResultRow> {
    val rows =  batchUpsert(data = list, keys = arrayOf(id), onUpdate = onUpdate, onUpdateExclude = onUpdateExclude, where = where, shouldReturnGeneratedValues = shouldReturnGeneratedValues) {
      data: SnowflakeSlot-> assign(this, data)
    }
    return rows
  }

  public fun SnowflakeSlotTable.update(
    raw: SnowflakeSlot,
    selective: Array<out Column<*>>? = null,
    ignore: Array<out Column<*>>? = null,
    limit: Int? = null,
    `where`: SqlExpressionBuilder.() -> Op<Boolean>,
  ): Int = update(`where`, limit) {
    val ignoreColumns = ignore ?: arrayOf()
    assign(it, raw, selective = selective, *ignoreColumns)
  }

  public fun SnowflakeSlotTable.updateByPrimaryKey(raw: SnowflakeSlot, vararg selective: Column<*>): Int = update(raw, selective = selective, ignore = arrayOf(id)) {
    SnowflakeSlotTable.id.eq(raw.id)
  }

  public fun SnowflakeSlotTable.updateByPrimaryKey(id: String, builder: SnowflakeSlotTable.(UpdateStatement) -> Unit): Int = update({ SnowflakeSlotTable.id.eq(id) }, body = builder)

  public fun SnowflakeSlotTable.deleteByPrimaryKey(id: String): Int = deleteWhere {
    SnowflakeSlotTable.id.eq(id)
  }

  public fun SnowflakeSlotTable.selectByPrimaryKey(id: String, vararg selective: Column<*>): SnowflakeSlot? {
    val query = selectSlice(*selective).andWhere {
      SnowflakeSlotTable.id.eq(id)
    }
    return query.firstOrNull()?.toSnowflakeSlot(*selective)
  }

  public fun SnowflakeSlotTable.selectByPrimaryKeys(ids: Iterable<String>, vararg selective: Column<*>): List<SnowflakeSlot> {
    val query = selectSlice(*selective).andWhere {
      SnowflakeSlotTable.id inList ids
    }
    return query.toSnowflakeSlotList(*selective)
  }

  public fun SnowflakeSlotTable.selectMany(vararg selective: Column<*>, `where`: Query.() -> Query?): List<SnowflakeSlot> {
    val query = selectSlice(*selective)
    `where`.invoke(query)
    return query.toSnowflakeSlotList(*selective)
  }

  public fun SnowflakeSlotTable.selectOne(vararg selective: Column<*>, `where`: Query.() -> Query?): SnowflakeSlot? {
    val query = selectSlice(*selective)
    `where`.invoke(query)
    return query.firstOrNull()?.toSnowflakeSlot(*selective)
  }

  public fun SnowflakeSlotTable.selectForwardByPrimaryKey(
    forwardToken: String? = null,
    order: SortOrder = SortOrder.DESC,
    pageSize: Int = 50,
    selective: Collection<Column<*>> = listOf(),
    `where`: (Query.() -> Query?)? = null,
  ): OffsetList<SnowflakeSlot> {
    if(pageSize < 1) {
      return OffsetList.empty()
    }
    val offsetKey = forwardToken?.let { OffsetList.decodeToken(it).firstOrNull()?.ifBlank { null } }
    val query = selectSlice(*selective.toTypedArray())
    offsetKey?.let {
      val keyValue = parseColumnValue(it, id)
      when(order) {
        SortOrder.DESC, SortOrder.DESC_NULLS_FIRST, SortOrder.DESC_NULLS_LAST->
        query.andWhere { id less keyValue }
        else-> query.andWhere { id greater keyValue }
      }
    }
    `where`?.invoke(query)
    val sorted = query.orderBy(id, order)
    val list = sorted.limit(pageSize + 1).toSnowflakeSlotList(*selective.toTypedArray()).toMutableList()
    val dataCount = list.size
    val token = if(dataCount > pageSize) {
      list.removeLast()
      val idString = list.last().getColumnValueString(id)
      OffsetList.encodeToken(idString)
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
    `where`: (Query.() -> Query?)? = null,
  ): OffsetList<SnowflakeSlot> {
    if(pageSize < 1) {
      return OffsetList.empty()
    }
    if(sortColumn == id) {
      return this.selectForwardByPrimaryKey(forwardToken, order, pageSize, selective, `where`)
    }
    val kp = forwardToken?.let { if(it.isNotBlank()) OffsetList.decodeToken(it) else null }
    val offsetKey = if(!kp.isNullOrEmpty()) parseColumnValue(kp.first(), sortColumn) else null
    val lastId = if(kp != null && kp.size > 1 && kp[1].isNotBlank()) parseColumnValue(kp[1], id) else null
    val query = selectSlice(*selective.toTypedArray())
    offsetKey?.let {
      when(order) {
        SortOrder.DESC, SortOrder.DESC_NULLS_FIRST, SortOrder.DESC_NULLS_LAST->
        query.andWhere { sortColumn lessEq it }
        else-> query.andWhere { sortColumn greaterEq it }
      }
    }
    lastId?.let {
      when(order) {
        SortOrder.DESC, SortOrder.DESC_NULLS_FIRST, SortOrder.DESC_NULLS_LAST->
        query.andWhere { id less it }
        else-> query.andWhere { id greater it }
      }
    }
    `where`?.invoke(query)
    val sorted = query.orderBy(Pair(sortColumn, order), Pair(id, order))
    val list = sorted.limit(pageSize + 1).toSnowflakeSlotList(*selective.toTypedArray()).toMutableList()
    val dataCount = list.size
    val token = if(dataCount > pageSize) {
      list.removeLast()
      val idToEncode = list.last().getColumnValueString(id)
      val sortKey = list.last().getColumnValueString(sortColumn)
      OffsetList.encodeToken(arrayOf(sortKey, idToEncode))
    }
    else null
    return OffsetList(list, token)
  }

  public fun SnowflakeSlotTable.replace(raw: SnowflakeSlot): ReplaceStatement<Long> = replace {
    assign(it, raw)
  }
}
