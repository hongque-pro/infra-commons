package com.labijie.infra.commons.snowflake.providers

import com.labijie.infra.commons.snowflake.ISlotProvider
import com.labijie.infra.commons.snowflake.SnowflakeException
import com.labijie.infra.commons.snowflake.WorkNodeInfo
import com.labijie.infra.commons.snowflake.configuration.SnowflakeConfig
import com.labijie.infra.spring.configuration.NetworkConfig
import com.labijie.infra.spring.configuration.getApplicationName
import com.labijie.infra.spring.configuration.isDevelopment
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
        private val isDevelopment:Boolean,
        private val networkConfig: NetworkConfig,
        private val snowflakeConfig: SnowflakeConfig) : ISlotProvider, AutoCloseable {

    constructor(environment: Environment, networkConfig: NetworkConfig, snowflakeConfig: SnowflakeConfig):
            this(environment.getApplicationName(), environment.isDevelopment, networkConfig, snowflakeConfig)


    private var client: CuratorFramework? = null
    var maxSlotCount: Int = 1024

    private var startupLock = Any()
    private var isStarted = false
    private var startWaiter: CountDownLatch? = CountDownLatch(1)
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
        val retryPolicy = RetryForever(sleepMs) //断线重连策略，这里使用仅重试一次的策略
        client = CuratorFrameworkFactory.builder()
                .connectString(snowflakeConfig.zk.server)
                .sessionTimeoutMs(Math.max(snowflakeConfig.zk.sessionTimeoutMs, TimeUnit.SECONDS.toMillis(20).toInt())) //1小时节点超时
                .retryPolicy(retryPolicy)
                .namespace("snowflake_slots")
                .build()

        this.client!!.connectionStateListenable.addListener(this.connectionStateListener);
        this.client!!.start()
        if (waitTimeoutMs != null) {
            if (!this.startWaiter!!.await(waitTimeoutMs, TimeUnit.MILLISECONDS)) {
                throw SnowflakeException("connect to zookeeper timeout, after $waitTimeoutMs ms, still not receiving the zookeeper server (${snowflakeConfig.zk.server}) response.")
            }
        } else {
            this.startWaiter!!.await()
        }
        this.startWaiter = null
        return this.client!!
    }


    fun connect(timeoutMs: Long = 15000L): CuratorFramework? {

        if (!isStarted) {
            synchronized(startupLock) {
                if (!isStarted) {
                    validateConfig()
                    try {
                        this.client = this.createClient(timeoutMs)
                    } catch (ex: SnowflakeException) {
                        if (this.isDevelopment) {
                            logger.warn("Because the current application profile is \"${Constants.LocalProfile}\" or \"${Constants.DevelopmentProfile}\", snowflake will ignore obtaining slot from the zookeeper.")
                            isStarted = true
                            return null;
                        } else {
                            throw ex
                        }
                    }
                    logger.info("Waiting for ZookeeperSlotProvider connect to zookeeper server...")
                    try {
                        this.client!!.create().withMode(CreateMode.PERSISTENT).forPath("/${snowflakeConfig.scope}", byteArrayOf(1))
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

        val client = this.connect() ?: return 1
        val ipAddress = networkConfig.getIPAddress(throwIfNotFound = !isDevelopment)
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