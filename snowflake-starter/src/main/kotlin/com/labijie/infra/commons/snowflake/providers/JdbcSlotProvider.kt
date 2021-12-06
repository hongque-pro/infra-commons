package com.labijie.infra.commons.snowflake.providers

import com.labijie.infra.SecondIntervalTimeoutTimer
import com.labijie.infra.commons.snowflake.ISlotProvider
import com.labijie.infra.commons.snowflake.SnowflakeException
import com.labijie.infra.commons.snowflake.configuration.InstanceIdentity
import com.labijie.infra.commons.snowflake.configuration.SnowflakeProperties
import com.labijie.infra.commons.snowflake.jdbc.SnowflakeSlotTable
import com.labijie.infra.commons.snowflake.jdbc.pojo.SnowflakeSlot
import com.labijie.infra.commons.snowflake.jdbc.pojo.dsl.SnowflakeSlotDSL.insert
import com.labijie.infra.commons.snowflake.jdbc.pojo.dsl.SnowflakeSlotDSL.selectByPrimaryKey
import com.labijie.infra.scheduling.IntervalTask
import com.labijie.infra.spring.configuration.NetworkConfig
import io.netty.util.Timeout
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.transaction.support.TransactionTemplate
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicLong

/**
 *
 * @Auther: AndersXiao
 * @Date: 2021/12/6
 * @Description:
 */
class JdbcSlotProvider constructor(
    maxSlot: Int,
    snowflakeConfig: SnowflakeProperties,
    networkConfig: NetworkConfig,
    tranTemplate: TransactionTemplate) : ISlotProvider, AutoCloseable {

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
        val log = LoggerFactory.getLogger(JdbcSlotProvider::class.java)
    }

    private val transactionTemplate = tranTemplate.configure(isolationLevel = Isolation.SERIALIZABLE)

    constructor(
        snowflakeConfig: SnowflakeProperties,
        networkConfig: NetworkConfig,
        transactionTemplate: TransactionTemplate
    ) :
            this(
                1024,
                snowflakeConfig,
                networkConfig,
                transactionTemplate
            )

    private val jdbcSlotProviderProperties = snowflakeConfig.jdbc

    private var maxSlotCount = maxSlot
    private var scope = snowflakeConfig.scope

    private val ipAddr = networkConfig.getIPAddress()
    private var task: IntervalTask? = null
    private val renewCount = AtomicLong()

    fun getRenewCount(): Long {
        return renewCount.get()
    }

    @Volatile
    private var stopped = false
    var slot: Short? = null


    fun getSlotValue(slot: Short): String {
        return "$scope:$slot"
    }

    val instanceId: String by lazy {
        when (jdbcSlotProviderProperties.instanceIdentity) {
            InstanceIdentity.IP -> ipAddr
            else -> UUID.randomUUID().toString().replace("-", "")
        }
    }


    private fun getTimeExpired(): Long = System.currentTimeMillis() + jdbcSlotProviderProperties.timeout.toMillis()

    override fun acquireSlot(throwIfNoneSlot: Boolean): Int? {
        val slotGot = acquire()
        if (slotGot != null) {
            log.info("Jdbc snowflake slot '$slotGot' retained by instance '$instanceId' ( ip: $ipAddr ) .")
            val interval = (jdbcSlotProviderProperties.timeout.toMillis() / 2.5).toLong()
            if (interval < 500) {
                log.warn("Jdbc snowflake slot timeout too short, current timeout: ${jdbcSlotProviderProperties.timeout}.")
            }
            this.task?.cancel()
            this.slot = slotGot.toShort()
            SecondIntervalTimeoutTimer.interval(Duration.ofMillis(interval), this::updateTimeExpired)
        } else if (throwIfNoneSlot) {
            throw SnowflakeException("There is no available slot for snowflake.")
        }
        return slotGot
    }

    private fun updateTimeExpired(t: Timeout?): SecondIntervalTimeoutTimer.TaskResult {
        if (this.stopped) {
            return SecondIntervalTimeoutTimer.TaskResult.Break
        }
        val expired = getTimeExpired()
        val slotValue = getSlotValue(slot ?: throw RuntimeException("slot value is null currently, jdbc slot update task fault."))

        transactionTemplate.execute {

            val count = SnowflakeSlotTable.update({ SnowflakeSlotTable.id eq slotValue }) {
                it[timeExpired] = expired
            }

            if (count > 0) {
                renewCount.incrementAndGet()
                if (log.isDebugEnabled) {
                    log.debug("Jdbc snowflake slot '$slotValue' updated.")
                }
            } else {
                log.error("unable to renew jdbc snowflake slot '$slotValue' for instance '$instanceId'.")
            }
        }
        return SecondIntervalTimeoutTimer.TaskResult.Continue
    }

    private fun acquire(): Int? {
        var latestSlot: Short = 1

        while (latestSlot <= maxSlotCount) {
            val (record, isNew) = getOrCreateSlot(latestSlot)
            if (isNew) {
                return latestSlot.toInt()
            }
            if (record.instance != this.instanceId && record.timeExpired > System.currentTimeMillis()) {
                latestSlot++
                continue
            }

            /*
           @Update("update core_snowflake_slots set " +
           "instance=#{instanceId}, addr=#{addr}, time_expired=#{timeExpired} " +
           "where slot_number=#{slotNumber} and (instance=#{instanceId} or time_expired <= #{nowTime})")
            */

            val slotId = getSlotValue(latestSlot)

            val count = transactionTemplate.execute {
                SnowflakeSlotTable.update({
                    (SnowflakeSlotTable.id eq slotId) and
                    (SnowflakeSlotTable.instance eq instanceId) or (SnowflakeSlotTable.timeExpired lessEq System.currentTimeMillis())
                }){
                    it[instance] = instanceId
                    it[address] = ipAddr
                    it[timeExpired] = getTimeExpired()
                }
            } ?: 0

            if (count == 1) {
                return latestSlot.toInt()
            }

            latestSlot++
        }
        return null
    }

    /**
     * @return record, isNew
     */
    private fun getOrCreateSlot(latestId: Short): Pair<SnowflakeSlot, Boolean> {
        val slotValue = getSlotValue(latestId)

        var isNew = false
        var r = transactionTemplate.configure(isReadOnly = true, isolationLevel = Isolation.SERIALIZABLE).execute {
            SnowflakeSlotTable.selectByPrimaryKey(slotValue)
        }

        if (r == null) {
            r = try {
                val record = SnowflakeSlot().apply {
                    id = slotValue
                    instance =instanceId
                    address = ipAddr
                    timeExpired = getTimeExpired()
                }

                transactionTemplate.execute {
                    SnowflakeSlotTable.insert(record)
                }
                isNew = true
                record
            } catch (e: DuplicateKeyException) {
                log.debug(e.toString())
                val existed =
                    transactionTemplate.execute {
                        SnowflakeSlotTable.selectByPrimaryKey(slotValue)
                    }
                existed
            }
        }

        return Pair(r!!, isNew)
    }

    override fun close() {
        this.stopped = true
        this.task?.cancel()
        val id = this.instanceId
        val count = transactionTemplate.execute {
            SnowflakeSlotTable.deleteWhere {
                SnowflakeSlotTable.instance eq id
            }
        }
        log.info("$count jdbc snowflake slot has been released.")
    }
}