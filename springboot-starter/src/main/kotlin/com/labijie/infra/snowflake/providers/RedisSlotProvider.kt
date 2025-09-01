package com.labijie.infra.snowflake.providers

import io.lettuce.core.*
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.codec.StringCodec
import io.lettuce.core.masterreplica.MasterReplica
import io.netty.util.Timeout
import com.labijie.infra.SecondIntervalTimeoutTimer
import com.labijie.infra.CommonsProperties
import com.labijie.infra.getApplicationName
import com.labijie.infra.isDevelopment
import com.labijie.infra.snowflake.ISlotProvider
import com.labijie.infra.snowflake.SnowflakeBitsConfig
import com.labijie.infra.snowflake.SnowflakeException
import com.labijie.infra.snowflake.SnowflakeProperties
import com.labijie.infra.utils.throwIfNecessary
import com.labijie.infra.utils.toLocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import java.time.Duration
import java.time.Instant
import java.util.*


/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-09-09
 */
class RedisSlotProvider(
    private val applicationName: String,
    private val isDevelopment: Boolean,
    private val commonsProperties: CommonsProperties,
    private val snowflakeConfig: SnowflakeProperties
) : ISlotProvider, AutoCloseable {


    constructor(environment: Environment, commonsProperties: CommonsProperties, snowflakeConfig: SnowflakeProperties)
            : this(environment.getApplicationName(), environment.isDevelopment, commonsProperties, snowflakeConfig)


    companion object {
        private val logger by lazy {
            LoggerFactory.getLogger(RedisSlotProvider::class.java)
        }

        var maxSlotCount: Int = 1024

        private val locker = Any()

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

    var maxSlotsCount = SnowflakeBitsConfig.DEFAULT_MACHINES_PER_CENTER

    override fun setMaxSlots(maxSlots: Int) {
        this.maxSlotsCount = maxSlots
    }


    init {
        if (redisConfig.sessionTimeout.toSeconds() < 180) {
            throw SnowflakeException("Redis slot timeout must not be less than 3 minute, please check redis sessionTimeout config.")
        }
    }


    private fun createClientAndConnection(url: String): StatefulRedisConnection<String, String> {
        if (url.isBlank()) {
            throw RedisException("Redis url cant not be null or empty string.")
        }
        val urls = url.split(",")
        return if (urls.size <= 1) {
            val uri = RedisURI.create(url)
            uri.timeout = Duration.ofSeconds(10)
            val c = RedisClient.create(uri)
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
    override fun acquireSlot(throwIfNoneSlot: Boolean): Int? {

        return synchronized(locker) {
            var result = AcquireResult.Failed
            for (i in 1..maxSlotCount) {
                result = tryAcquireSlot(i)
                if (result == AcquireResult.Success) {
                    startRefreshTask(i)
                    return@synchronized i
                }
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
            return@synchronized null
        }
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
                    val result =
                        it.sync().eval<Long>(UNLOCK_LUA_SCRIPT, ScriptOutputType.INTEGER, arrayOf(key), getValue())
                    if (result == 1L) {
                        dealLine = 0
                        currentSlot = -1
                        timeOut?.cancel()
                        timeOut = null
                        logger.info("Snowflake slot (redis) has been released, scope:${snowflakeConfig.scope}, slot: '$key'.")
                    }
                }
            } catch (e: RedisException) {
                logger.warn("Release snowflake slot fault (redis).", e)
            }
        }
    }


    private fun tryAcquireSlot(slot: Int): AcquireResult {

        val connection = try {
            createClientAndConnection(this.redisConfig.url)
        } catch (_: RedisConnectionException) {
            return AcquireResult.RedisError
        } catch (e: Throwable) {
            logger.error("Acquire slot failed (redis).", e)
            e.throwIfNecessary()
            return AcquireResult.RedisError
        }

        connection.use {
            connection.timeout = Duration.ofSeconds(10)
            val command = connection.sync()
            AutoCloseable { }
            val result = setSlot(slot, command)
            return result
        }
    }

    private fun startRefreshTask(slot: Int) {
        //保证一个 session 周期内至少有 3 次重试机会
        val refreshDuration = redisConfig.sessionTimeout.dividedBy(3).toMillis()
        timeOut = SecondIntervalTimeoutTimer.newTimeout(refreshDuration) {
            val result = this.tryAcquireSlot(slot)
            val isDeadlineExceeded = (System.currentTimeMillis() + refreshDuration) > dealLine
            when (result) {
                AcquireResult.Failed, AcquireResult.RedisError -> {
                    if (isDeadlineExceeded) {
                        logger.error("Snowflake slot $slot will be timeout and cant refresh to server, process forced exit (slot provider: redis) !!")
                        System.exit(9999)
                    }
                }

                AcquireResult.Success -> {

                }
            }
            startRefreshTask(slot)
        }
    }

    private fun setSlot(i: Int, command: RedisCommands<String, String>): AcquireResult {
        val timeout = redisConfig.sessionTimeout.toSeconds().toString()
        val value = getValue()
        val key = getSlotKey(i)
        var retriedCount = 0
        var result: AcquireResult = AcquireResult.Failed
        while (retriedCount <= 3) {
            try {
                val r = command.eval<Long>(LOCK_LUA_SCRIPT, ScriptOutputType.INTEGER, arrayOf(key), value, timeout)
                result = if (r == 1L) {
                    dealLine = System.currentTimeMillis() + redisConfig.sessionTimeout.toMillis()
                    if (logger.isDebugEnabled) {

                        val date = Instant.ofEpochMilli(dealLine).toLocalDateTime()
                        logger.debug("Redis slot $i has been refreshed, next deadline (UTC): $date")
                    }
                    currentSlot = i
                    AcquireResult.Success
                } else {
                    AcquireResult.Failed
                }
                return result
            } catch (e: RedisException) {
                result = AcquireResult.RedisError
                logger.warn("Request snowflake slot fault.", e)
                Thread.sleep(1000)
                retriedCount++
            }
        }
        return result
    }

    private fun getValue() = "$applicationName:${this.commonsProperties.getIPAddress()}:$instanceStamp"

    private fun getSlotKey(i: Int): String {
        return "snowflake:${snowflakeConfig.fixedScope(":")}:slot:$i"
    }


    private enum class AcquireResult {
        Success,
        Failed,
        RedisError
    }
}