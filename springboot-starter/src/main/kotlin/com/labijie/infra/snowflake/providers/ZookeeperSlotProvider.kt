package com.labijie.infra.snowflake.providers

import com.labijie.infra.CommonsProperties
import com.labijie.infra.getApplicationName
import com.labijie.infra.isDevelopment
import com.labijie.infra.snowflake.*
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.framework.state.ConnectionState
import org.apache.curator.framework.state.ConnectionStateListener
import org.apache.curator.retry.RetryForever
import org.apache.zookeeper.CreateMode
import org.apache.zookeeper.KeeperException.NodeExistsException
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import java.io.IOException
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-10
 */
class ZookeeperSlotProvider(
    applicationName: String,
    private val isDevelopment: Boolean,
    private val commonsProperties: CommonsProperties,
    private val snowflakeConfig: SnowflakeProperties
) : ISlotProvider, AutoCloseable {

    constructor(
        environment: Environment,
        commonsProperties: CommonsProperties,
        snowflakeConfig: SnowflakeProperties
    ) : this(
        environment.getApplicationName(),
        environment.isDevelopment,
        commonsProperties,
        snowflakeConfig
    )


    companion object {
        private val logger by lazy {
            LoggerFactory.getLogger(ZookeeperSlotProvider::class.java)
        }
    }

    private var client: CuratorFramework? = null
    var maxSlotCount: Int = SnowflakeBitsConfig.DEFAULT_MACHINES_PER_CENTER

    private val startupLock = Any()
    var isStarted = false
        private set
    private var startWaiter: CountDownLatch? = null
    private val serviceName = applicationName
    private val instanceStamp = UUID.randomUUID().toString()

    override fun setMaxSlots(maxSlots: Int) {
        this.maxSlotCount = maxSlots
    }

    private val connectionStateListener = ConnectionStateListener { _, newState ->
        when (newState) {
            ConnectionState.RECONNECTED, ConnectionState.CONNECTED -> {
                logger.info("Snowflake slot provider connected to Zookeeper successfully.")
                startWaiter?.countDown()
            }
            ConnectionState.LOST -> {
                logger.warn("Zookeeper connection lost. Waiting to reconnect...")
            }
            else -> {}
        }
    }

    private fun validateConfig() {
        if (snowflakeConfig.zk.server.isBlank()) {
            throw SnowflakeException("Zookeeper server address must be configured for Snowflake ID generator.")
        }
        if (snowflakeConfig.scope.isBlank()) {
            throw SnowflakeException("Snowflake ID scope must not be blank.")
        }
        if (!Regex("^[a-z0-9_-]{1,32}$").matches(snowflakeConfig.scope)) {
            throw SnowflakeException("Snowflake scope must be 1-32 characters, only lowercase letters, numbers, '_' or '-'.")
        }
    }

    @Throws(SnowflakeException::class)
    private fun createClient(waitTimeoutMs: Long?): CuratorFramework {
        val retryPolicy = RetryForever(10_000) // 永远重连
        val c = CuratorFrameworkFactory.builder()
            .connectString(snowflakeConfig.zk.server)
            .sessionTimeoutMs(
                snowflakeConfig.zk.sessionTimeout.toMillis().toInt()
                    .coerceAtLeast(20_000)
            )
            .retryPolicy(retryPolicy)
            .namespace("snowflake_slots")
            .build()
        c.connectionStateListenable.addListener(this.connectionStateListener)

        this.startWaiter = CountDownLatch(1)
        try {
            c.start()
            waitTimeoutMs?.let {
                if (!this.startWaiter!!.await(it, TimeUnit.MILLISECONDS)) {
                    c.close()
                    throw SnowflakeException("Timeout connecting to Zookeeper after $it ms.")
                }
            } ?: this.startWaiter!!.await()
        } finally {
            this.startWaiter = null
        }
        return c
    }

    private fun connect(timeoutMs: Long = 15_000L): CuratorFramework? {
        if (!isStarted) {
            synchronized(startupLock) {
                if (!isStarted) {
                    validateConfig()
                    try {
                        this.client = this.createClient(timeoutMs)
                    } catch (ex: SnowflakeException) {
                        if (isDevelopment) {
                            logger.error(
                                "Development profile detected. Snowflake will skip obtaining slot from Zookeeper. Reason: ${ex.message}"
                            )
                            return null
                        } else throw ex
                    }

                    // 创建持久化根节点
                    val rootPath = "/${snowflakeConfig.fixedScope("/")}".replace("//", "/")
                    try {
                        client!!.create().withMode(CreateMode.PERSISTENT).forPath(rootPath, byteArrayOf(1))
                    } catch (_: NodeExistsException) {
                        // 忽略
                    }

                    logger.info("ZookeeperSlotProvider connected successfully. Root path: $rootPath")
                    isStarted = true
                }
            }
        }
        return client
    }

    @Throws(SnowflakeException::class)
    override fun acquireSlot(throwIfNoneSlot: Boolean): Int? {
        val client = this.connect(snowflakeConfig.zk.connectTimeout.toMillis().coerceAtLeast(5_000)) ?: return 1
        val ipAddress = commonsProperties.getIPAddress(throwIfNotFound = !isDevelopment)
        val currentNode = WorkNodeInfo(ipAddress, serviceName, instanceStamp)

        for (i in 1..maxSlotCount) {
            val path = "/${snowflakeConfig.fixedScope("/")}/slot_$i".replace("//", "/")
            try {
                client.create().withMode(CreateMode.EPHEMERAL).forPath(path, currentNode.toBytes())
                logger.info("Snowflake slot registered successfully: scope='${snowflakeConfig.scope}', slot='$path', instanceStamp=$instanceStamp")
                return i
            } catch (ex: NodeExistsException) {
                // 节点存在
                try {
                    val data = client.data.forPath(path)
                    val info = WorkNodeInfo.fromBytes(data)
                    if (info.instanceStamp == currentNode.instanceStamp) {
                        // 是自己残留，可以安全删除并重试
                        client.delete().forPath(path)
                        continue
                    }
                    // 被其他实例占用，跳过
                } catch (_: IOException) {
                    logger.warn("Cannot deserialize ZK node data at path '$path'. Skipping slot.")
                }
                continue
            }
        }

        if (throwIfNoneSlot) {
            throw SnowflakeException("No available slot for Snowflake in scope '${snowflakeConfig.scope}'.")
        }
        return null
    }

    override fun close() = disconnect()

    fun disconnect() {
        if (isStarted) {
            synchronized(startupLock) {
                if (isStarted) {
                    client?.close()
                    client = null
                    isStarted = false
                }
            }
        }
    }

}