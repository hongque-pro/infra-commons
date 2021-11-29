package com.labijie.infra.commons.snowflake.providers

import io.lettuce.core.*
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.codec.StringCodec
import io.lettuce.core.masterreplica.MasterReplica
import io.netty.util.Timeout
import com.labijie.infra.SecondIntervalTimeoutTimer
import com.labijie.infra.commons.snowflake.ISlotProvider
import com.labijie.infra.commons.snowflake.SnowflakeException
import com.labijie.infra.commons.snowflake.configuration.SnowflakeConfig
import com.labijie.infra.spring.configuration.NetworkConfig
import com.labijie.infra.spring.configuration.getApplicationName
import com.labijie.infra.spring.configuration.isDevelopment
import com.labijie.infra.utils.toLocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.system.exitProcess


/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-09-09
 */
class RedisSlotProvider(
        private val applicationName: String,
        private val isDevelopment: Boolean,
        private val networkConfig: NetworkConfig,
        private val snowflakeConfig: SnowflakeConfig) : ISlotProvider, AutoCloseable {


    constructor(environment: Environment, networkConfig: NetworkConfig, snowflakeConfig: SnowflakeConfig)
            : this(environment.getApplicationName(), environment.isDevelopment, networkConfig, snowflakeConfig)


    companion object {
        private val log = LoggerFactory.getLogger(RedisSlotProvider::class.java)

        var maxSlotCount: Int = 1024

        private const val LOCK_LUA_SCRIPT =
                "if ((redis.call('setnx',KEYS[1],ARGV[1]) == 1) or (redis.call('get',KEYS[1]) == ARGV[1])) then " +
                        "redis.call('expire',KEYS[1],ARGV[2]) return 1 " +
                        "else return 0 " +
                        "end"

        private const val UNLOCK_LUA_SCRIPT = "if redis.call('get',KEYS[1]) == ARGV[1] then " +
                "return redis.call('del',KEYS[1]) else return 0 end"

    }


    private val redisConfig = snowflakeConfig.redis

    private val instanceStamp: String = UUID.randomUUID().toString()
    @Volatile
    private var hooked: Boolean = false

    //最迟需要刷新的时间
    @Volatile
    private var dealLine: Long = 0

    var currentSlot = -1
        private set
    private var timeOut: Timeout? = null

    init {
        if (redisConfig.sessionTimeout.seconds < 180) {
            throw SnowflakeException("Redis slot timeout must not be less than 3 minute, please check redis sessionTimeout config.")
        }
    }


    private fun createClientAndConnection(url: String): StatefulRedisConnection<String, String> {
        if (url.isBlank()) {
            throw RedisException("Redis url cant not be null or empty string.")
        }
        val urls = url.split(",")
        return if (urls.size <= 1) {
            val c = RedisClient.create(url)
            c.defaultTimeout = Duration.ofSeconds(10)
            c.connect()
        } else {
            val redisUrls = urls.map {
                RedisURI.create(it.trim())
            }
            val client = RedisClient.create()
            val connection = MasterReplica.connect(
                    client, StringCodec(),
                    redisUrls
            )
            connection.readFrom = ReadFrom.MASTER_PREFERRED
            connection
        }
    }

    @Throws(SnowflakeException::class)
    @Synchronized
    override fun acquireSlot(throwIfNoneSlot: Boolean): Int? {

        var result = AcquireResult.Failed
        for (i in 1..maxSlotCount) {
            result = tryAcquireSlot(i)
            if (result == AcquireResult.Success) return i
            if (result == AcquireResult.RedisError) {
                break
            }
        }

        if (throwIfNoneSlot) {
            when (result) {
                AcquireResult.RedisError -> throw SnowflakeException("A redis error occurred when request snowflake slot.")
                else -> throw SnowflakeException("There is no available slot for snowflake.")
            }
        }

        hoodShutdown()
        return null
    }

    private fun hoodShutdown() {
        if (!hooked) {
            hooked = true
            Runtime.getRuntime().addShutdownHook(Thread {
                close()
            })
        }
    }

    override fun close() {
        if (currentSlot > 0) {
            try {
                val conn = createClientAndConnection(this.redisConfig.url)
                conn.use {
                    val key = getSlotKey(currentSlot)
                    val result = it.sync().eval<Long>(UNLOCK_LUA_SCRIPT, ScriptOutputType.INTEGER, arrayOf(key), getValue())
                    if (result == 1L) {
                        dealLine = 0
                        currentSlot = -1
                        timeOut?.cancel()
                        log.info("Snowflake slot (redis) has been released, scope:${snowflakeConfig.scope}, slot: '$key'.")
                    }
                }
            } catch (e: RedisException) {
                log.warn("Release snowflake slot fault (redis).", e)
            }
        }
    }


    private fun tryAcquireSlot(slot: Int): AcquireResult {

        val connection = try {
            createClientAndConnection(this.redisConfig.url)
        } catch (e: RedisConnectionException) {
            return AcquireResult.RedisError
        }

        connection.use {
            val command = connection.sync()
            command.setTimeout(Duration.ofSeconds(10))
            AutoCloseable {  }
            val result = setSlot(slot, command)
            when (result) {
                AcquireResult.Success -> {
                    log.info("Snowflake slot (redis) registering was done, scope:${snowflakeConfig.scope}, slot: '${getSlotKey(slot)}'.")
                    startRefreshTask(slot)
                }
                else -> {
                }
            }

            return result
        }
    }

    private fun startRefreshTask(slot: Int) {
        //保证一个 session 周期内至少有 3 次重试机会
        val refreshDuration = redisConfig.sessionTimeout.dividedBy(3).toMillis()
        timeOut = SecondIntervalTimeoutTimer.newTimeout(refreshDuration) {
            val result = this.tryAcquireSlot(slot)
            val outOfDeadLine = (System.currentTimeMillis() + refreshDuration) > dealLine
            when (result) {
                AcquireResult.Failed -> {
                    log.error("When redis slot provider refresh a slot, it found that the slot is occupied by another instance (normally this shouldn't happen !!)")
                    exitProcess(-9999)
                }
                AcquireResult.RedisError -> {
                    if (outOfDeadLine) {
                        log.error("Snowflake slot $slot will be timeout and cant refresh to server, process forced exit !!")
                        exitProcess(-9999)
                    } else {
                        this.startRefreshTask(slot)
                    }
                }
                else -> {
                }
            }
        }
    }

    private fun setSlot(i: Int, command: RedisCommands<String, String>): AcquireResult {
        val timeout = redisConfig.sessionTimeout.seconds.toString()
        val value = getValue()
        val key = getSlotKey(i)
        var retriedCount = 0
        var result: AcquireResult = AcquireResult.Failed
        while (retriedCount <= 3) {
            try {
                val r = command.eval<Long>(LOCK_LUA_SCRIPT, ScriptOutputType.INTEGER, arrayOf(key), value, timeout)
                result = if (r == 1L) {
                    dealLine = System.currentTimeMillis() + redisConfig.sessionTimeout.toMillis()
                    if (log.isDebugEnabled) {

                        val date = Instant.ofEpochMilli(dealLine).toLocalDateTime()
                        log.debug("Redis slot $i has been refreshed, next deadline (UTC): $date")
                    }
                    currentSlot = i
                    AcquireResult.Success
                } else {
                    AcquireResult.Failed
                }
                return result
            } catch (e: RedisException) {
                result = AcquireResult.RedisError
                log.warn("Request snowflake slot fault.", e)
                Thread.sleep(1000)
                retriedCount++
            }
        }
        return result
    }

    private fun getValue() = "$applicationName:${this.networkConfig.getIPAddress()}:$instanceStamp"

    private fun getSlotKey(i: Int): String {
        return "_snow_${snowflakeConfig.scope}:slot_$i"
    }


    private enum class AcquireResult {
        Success,
        Failed,
        RedisError
    }
}