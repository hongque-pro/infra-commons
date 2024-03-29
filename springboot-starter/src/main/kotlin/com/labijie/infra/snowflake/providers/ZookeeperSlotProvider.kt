package com.labijie.infra.snowflake.providers

import com.labijie.infra.CommonsProperties
import com.labijie.infra.getApplicationName
import com.labijie.infra.isDevelopment
import com.labijie.infra.snowflake.ISlotProvider
import com.labijie.infra.snowflake.SnowflakeException
import com.labijie.infra.snowflake.SnowflakeProperties
import com.labijie.infra.snowflake.WorkNodeInfo
import com.labijie.infra.utils.Constants
import com.labijie.infra.utils.logger
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.framework.state.ConnectionState
import org.apache.curator.framework.state.ConnectionStateListener
import org.apache.curator.retry.RetryForever
import org.apache.zookeeper.CreateMode
import org.apache.zookeeper.KeeperException.NodeExistsException
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

    constructor(environment: Environment, commonsProperties: CommonsProperties, snowflakeConfig: SnowflakeProperties) :
            this(environment.getApplicationName(), environment.isDevelopment, commonsProperties, snowflakeConfig)


    private var client: CuratorFramework? = null
    var maxSlotCount: Int = 1024

    private var startupLock = Any()
    var isStarted = false
        private set
    private var startWaiter: CountDownLatch? = null
    private val serviceName = applicationName
    private val instanceStamp = UUID.randomUUID().toString()


    private val connectionStateListener = ConnectionStateListener { _, newState ->
        when (newState) {
            ConnectionState.RECONNECTED,
            ConnectionState.CONNECTED -> {
                this.logger.info("Snowflake slot provider connection to zookeeper have been successful.")
                startWaiter?.countDown()
            }
            ConnectionState.LOST -> {
                this.logger.warn("Zookeeper connection for snowflake slot provider was lost,waiting for reconnecting.")
            }
            else -> {
            }
        }
    }

    @Throws(SnowflakeException::class)
    private fun createClient(waitTimeoutMs: Long?): CuratorFramework {
        val sleepMs = 10000
        val retryPolicy = RetryForever(sleepMs) //断线重连，永远重连
        val c = CuratorFrameworkFactory.builder()
            .connectString(snowflakeConfig.zk.server)
            .sessionTimeoutMs(
                snowflakeConfig.zk.sessionTimeout.toMillis().toInt()
                    .coerceAtLeast(TimeUnit.SECONDS.toMillis(20).toInt())
            ) //1小时节点超时
            .retryPolicy(retryPolicy)
            .namespace("snowflake_slots")
            .build()
        c.connectionStateListenable.addListener(this.connectionStateListener)
        this.startWaiter = CountDownLatch(1)
        try {
            c.start()
            if (waitTimeoutMs != null) {
                if (!this.startWaiter!!.await(waitTimeoutMs, TimeUnit.MILLISECONDS)) {
                    c.close()
                    throw SnowflakeException("connect to zookeeper timeout, after $waitTimeoutMs ms, still not receiving the zookeeper server (${snowflakeConfig.zk.server}) response.")
                }
            } else {
                this.startWaiter!!.await()
            }
        } finally {
            this.startWaiter = null
        }
        return c
    }


    private fun connect(timeoutMs: Long = 15000L): CuratorFramework? {

        if (!isStarted) {
            synchronized(startupLock) {
                if (!isStarted) {
                    validateConfig()
                    try {
                        this.client = this.createClient(timeoutMs)
                    } catch (ex: SnowflakeException) {
                        if (this.isDevelopment) {
                            logger.error("Because the current application profile is \"${Constants.LocalProfile}\" or \"${Constants.DevelopmentProfile}\", snowflake will ignore obtaining slot from the zookeeper.")
                            //isStarted = true
                            return null
                        } else {
                            throw ex
                        }
                    }
                    logger.info("Waiting for ZookeeperSlotProvider connect to zookeeper server...")
                    try {
                        this.client!!.create().withMode(CreateMode.PERSISTENT)
                            .forPath("/${snowflakeConfig.scope}", byteArrayOf(1))
                    } catch (ex: NodeExistsException) {
                        //节点已经存在忽略该错误
                    }
                    logger.info("ZookeeperSlotProvider connect to zookeeper success.")
                    isStarted = true
                }
            }
        }
        return this.client
    }

    override fun close() = this.disconnect()

    fun disconnect() {
        if (isStarted) {
            synchronized(startupLock) {
                if (isStarted) {
                    this.client?.close()
                    this.client = null
                    this.isStarted = false
                }
            }
        }
    }

    @Throws(SnowflakeException::class)
    override fun acquireSlot(throwIfNoneSlot: Boolean): Int? {

        val client = this.connect(this.snowflakeConfig.zk.connectTimeout.toMillis().coerceAtLeast(5000)) ?: return 1
        val ipAddress = commonsProperties.getIPAddress(throwIfNotFound = !isDevelopment)
        val currentNode = WorkNodeInfo(ipAddress, this.serviceName, instanceStamp)
        for (i in 1..this.maxSlotCount) {
            val path = "/${snowflakeConfig.scope}/slot_$i"
            try {
                client.create().withMode(CreateMode.EPHEMERAL)
                    .forPath(path, currentNode.toBytes())
            } catch (ex: NodeExistsException) {
                val data = client.data.forPath(path)
                try {
                    val info = WorkNodeInfo.fromBytes(data)

                    //删除之后循环会继续寻找下一个（强制循环 continue），这样不需要考虑释放问题
                    if (info.instanceStamp == currentNode.instanceStamp) {
                        client.delete().forPath(path)
                    }
                } catch (ex: IOException) {
                    //格式错误，可能由于版本升级造成
                    this.logger.warn("Cant deserialize zk node data at path '$path'.")
                }
                continue
            }
            this.logger.info("Snowflake slot registering was done, scope:${snowflakeConfig.scope}, slot: '$path'.")
            return i
        }
        if (throwIfNoneSlot) {
            throw SnowflakeException("There is no available slot for snowflake.")
        }
        return null
    }

    private fun validateConfig() {
        if ((snowflakeConfig.zk.server).isBlank()) {
            throw SnowflakeException("The zookeeper server address must be configured for using snowflake id generator.")
        }

        if (snowflakeConfig.scope.isBlank()) {
            throw SnowflakeException("Snowflake id scope must not be null or empty string.")
        }

        if (!Regex("^[a-z0-9-]{1,32}\$").matches(snowflakeConfig.scope)) {
            throw SnowflakeException("The snowflake scope name can only contain lowercase letters, underscores numbers and length must be less than 32.")
        }
    }
}