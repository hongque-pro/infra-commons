package com.labijie.infra.snowflake.providers

import com.labijie.infra.SecondIntervalTimeoutTimer
import com.labijie.infra.scheduling.IntervalTask
import com.labijie.infra.snowflake.ISlotProvider
import com.labijie.infra.snowflake.SnowflakeBitsConfig
import com.labijie.infra.snowflake.SnowflakeException
import com.labijie.infra.snowflake.SnowflakeProperties
import com.labijie.infra.snowflake.config.InstanceIdentity
import com.labijie.infra.snowflake.jdbc.SnowflakeSlotTable
import com.labijie.infra.snowflake.jdbc.pojo.SnowflakeSlot
import com.labijie.infra.snowflake.jdbc.pojo.dsl.SnowflakeSlotDSL.insert
import com.labijie.infra.snowflake.jdbc.pojo.dsl.SnowflakeSlotDSL.selectByPrimaryKey
import com.labijie.infra.utils.throwIfNecessary
import io.netty.util.Timeout
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.transaction.support.TransactionTemplate
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicLong

/**
 *
 * @Auther: AndersXiao
 * @Date: 2021/12/6
 * @Description:
 */
class JdbcSlotProvider(
    maxSlot: Int,
    private val snowflakeConfig: SnowflakeProperties,
    commonsProperties: com.labijie.infra.CommonsProperties,
    tranTemplate: TransactionTemplate
) : ISlotProvider, AutoCloseable {

    companion object {
        private fun TransactionTemplate.configure(
            isReadOnly: Boolean = false,
            propagation: Propagation = Propagation.REQUIRED,
            isolationLevel: Isolation = Isolation.DEFAULT,
            timeout: Int = TransactionDefinition.TIMEOUT_DEFAULT
        ): TransactionTemplate {
            val definition = DefaultTransactionDefinition().apply {
                this.isReadOnly = isReadOnly
                this.isolationLevel = isolationLevel.value()
                this.propagationBehavior = propagation.value()
                this.timeout = timeout
            }
            return TransactionTemplate(this.transactionManager!!, definition)
        }

        @JvmStatic
        private val logger = LoggerFactory.getLogger(JdbcSlotProvider::class.java)
    }

    private val transactionTemplate = tranTemplate.configure(isolationLevel = Isolation.SERIALIZABLE)

    constructor(
        snowflakeConfig: SnowflakeProperties,
        commonsProperties: com.labijie.infra.CommonsProperties,
        transactionTemplate: TransactionTemplate
    ) :
            this(
                SnowflakeBitsConfig.DEFAULT_MACHINES_PER_CENTER,
                snowflakeConfig,
                commonsProperties,
                transactionTemplate
            )

    private val jdbcSlotProviderProperties = snowflakeConfig.jdbc

    private var maxSlotCount = maxSlot

    private val ipAddr = commonsProperties.getIPAddress()
    private var task: IntervalTask? = null
    private val renewCount = AtomicLong()

    @Volatile
    private var deadline: Long = 0

    /**
     * 订阅间隔 (心跳周期)
     */
    private val checkIntervalMills
        get() = (jdbcSlotProviderProperties.timeout.toMillis() / 3)


    fun getRenewCount(): Long {
        return renewCount.get()
    }

    @Volatile
    private var stopped = false
    var slot: Short? = null


    fun getSlotValue(slot: Short): String {
        return "${snowflakeConfig.fixedScope(":")}:$slot"
    }

    val instanceId: String by lazy {
        when (jdbcSlotProviderProperties.instanceIdentity) {
            InstanceIdentity.IP -> ipAddr
            else -> UUID.randomUUID().toString().replace("-", "")
        }
    }


    private fun getTimeExpired(): Long {
        return System.currentTimeMillis() + jdbcSlotProviderProperties.timeout.toMillis()
    }

    override fun acquireSlot(throwIfNoneSlot: Boolean): Int? {
        val slotGot = acquireFromDatabase()
        if (slotGot != null) {
            logger.info("Jdbc snowflake slot '$slotGot' retained by instance '$instanceId' ( ip: $ipAddr ) .")

            if (checkIntervalMills < 500) {
                logger.warn("Jdbc snowflake slot timeout too short, current timeout: ${jdbcSlotProviderProperties.timeout}.")
            }
            this.task?.cancel()
            this.slot = slotGot.toShort()
            SecondIntervalTimeoutTimer.interval(Duration.ofMillis(checkIntervalMills), this::updateTimeExpired)
        } else if (throwIfNoneSlot) {
            throw SnowflakeException("There is no available slot for snowflake.")
        }
        return slotGot
    }

    override fun setMaxSlots(maxSlots: Int) {
        this.maxSlotCount = maxSlots
    }

    @Suppress("UNUSED_PARAMETER")
    private fun updateTimeExpired(t: Timeout?): SecondIntervalTimeoutTimer.TaskResult {
        if (this.stopped) {
            return SecondIntervalTimeoutTimer.TaskResult.Break
        }
        val expired = getTimeExpired()
        val slotValue =
            getSlotValue(slot ?: throw RuntimeException("slot value is null currently, jdbc slot update task fault."))

        val success = transactionTemplate.execute {
            val count = SnowflakeSlotTable.update({ SnowflakeSlotTable.id eq slotValue }) {
                it[timeExpired] = expired
            }

            if (count > 0) {
                renewCount.incrementAndGet()
                if (logger.isDebugEnabled) {
                    logger.debug("Jdbc snowflake slot '$slotValue' updated.")
                }
                return@execute true
            }
            logger.error("unable to renew jdbc snowflake slot '$slotValue' for instance '$instanceId'.")
            false
        } ?: false

        if (success) {
            deadline = expired
            if (logger.isDebugEnabled) {
                logger.debug(
                    "Jdbc snowflake slot '$slotValue' renewed by $instanceId, next deadline=${
                        Instant.ofEpochMilli(
                            deadline
                        )
                    }"
                )
            }
        } else {
            if ((System.currentTimeMillis() + checkIntervalMills) > deadline) {
                logger.error("Unable to renew jdbc snowflake slot '$slotValue' for instance '$instanceId'. Deadline expired, aborting to avoid ID conflicts.")
                System.exit(9999)
            }
        }
        return SecondIntervalTimeoutTimer.TaskResult.Continue
    }

    private fun acquireFromDatabase(): Int? {
        var latestSlot: Short = 1
        while (latestSlot <= maxSlotCount) {
            val record = getOrCreateSlotSafe(latestSlot)

            // slot 已被其他实例占用且未过期
            if (record == null) {
                latestSlot++
                continue
            }

            deadline = record.timeExpired
            return latestSlot.toInt()
        }
        return null
    }

    private fun getOrCreateSlotSafe(slot: Short, maxAttempts: Int = 5): SnowflakeSlot? {
        val slotValue = getSlotValue(slot)
        var attempts = 0
        while (attempts < maxAttempts) {
            attempts++
            // 查询是否存在
            val existing = transactionTemplate.configure(isolationLevel = Isolation.SERIALIZABLE).execute {

                val timeout = System.currentTimeMillis() - 60_000 //延迟过期

                // update only if timeExpired <= now (expired)
                val newExpire = getTimeExpired()
                val count = SnowflakeSlotTable.update({
                    (SnowflakeSlotTable.id eq slotValue) and (SnowflakeSlotTable.timeExpired lessEq timeout)
                }) {
                    it[instance] = instanceId
                    it[address] = ipAddr
                    it[timeExpired] = newExpire
                }
                if (count > 0) {
                    // return the updated row
                    SnowflakeSlotTable.selectByPrimaryKey(slotValue)
                } else null
            }

            if(existing != null) {
                return existing
            }

            // 尝试插入
            try {
                val record = SnowflakeSlot().apply {
                    this.id = slotValue
                    this.instance = instanceId
                    this.address = ipAddr
                    this.timeExpired = getTimeExpired()
                }
                val count = transactionTemplate.execute {
                    SnowflakeSlotTable.insert(record).insertedCount
                } ?: 0

                if (count > 0) {
                    return record
                }
            } catch (_: DuplicateKeyException) {
                logger.debug("Concurrent insert detected for $slotValue, retrying")
                // small sleep then continue attempts
                Thread.sleep(10)
                continue
            } catch (e: Throwable) {
                //可能是数据库错误
                e.throwIfNecessary()
                logger.error("Unexpected error when inserting slot $slotValue, attempt $attempts", e)
            }

            // 小睡一段时间再重试，避免热点争用
            Thread.sleep(10)
        }

        throw SnowflakeException("Failed to acquire or create slot $slotValue after $maxAttempts attempts")
    }


    override fun close() {
        this.stopped = true
        this.task?.cancel()
        val id = this.instanceId
        val count = transactionTemplate.execute {
            SnowflakeSlotTable.deleteWhere {
                instance eq id
            }
        }
        logger.info("$count jdbc snowflake slot has been released.")
    }
}