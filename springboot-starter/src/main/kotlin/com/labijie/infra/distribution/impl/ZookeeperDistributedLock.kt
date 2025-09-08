package com.labijie.infra.distribution.impl

import com.labijie.infra.distribution.DistributedProperties
import com.labijie.infra.distribution.DistributionException
import com.labijie.infra.distribution.IDistributedLock
import com.labijie.infra.distribution.LockScope
import com.labijie.infra.getApplicationName
import com.labijie.infra.utils.throwIfNecessary
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.framework.recipes.locks.InterProcessLock
import org.apache.curator.framework.recipes.locks.InterProcessMutex
import org.apache.curator.framework.state.ConnectionStateListener
import org.apache.curator.retry.ExponentialBackoffRetry
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import java.lang.AutoCloseable
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit


/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-09-19
 */
class ZookeeperDistributedLock(private val environment: Environment, private val config: DistributedProperties) :
    IDistributedLock, AutoCloseable {

    companion object {
        fun throwIfInvalidName(name: String) {
            if (name.isBlank()) {
                throw IllegalArgumentException("DistributedLock name must not be null or empty string.")
            }
        }

        private val logger by lazy {
            LoggerFactory.getLogger("com.labijie.infra.distribution.impl.ZookeeperDistributedLock")
        }
    }

    val listeners: MutableList<ConnectionStateListener> = mutableListOf()
    private val applicationName = this.environment.getApplicationName().replace('/', '_')
    private var client: CuratorFramework? = null;
    private val clientSync: Any = Any()
    private val locks: ConcurrentHashMap<String, LockModel> = ConcurrentHashMap()

    private val listener: ConnectionStateListener = ConnectionStateListener { client, newState ->
        this.listeners.toTypedArray().forEach {
            try {
                it.stateChanged(client, newState)
            } catch (ex: Throwable) {
                logger.error("listen zookeeper status changed error.", ex)
            }
        }
    }

    init {
        Runtime.getRuntime().addShutdownHook(Thread {
            this.close()
            this.client?.close()
            this.client = null
        })
    }

    private fun createClient(config: DistributedProperties): CuratorFramework {
        if (config.zkServer.isBlank()) {
            throw DistributionException("To use distributed lock, you must configure the zookeeper server address (infra.distribution.zk-server)")
        }

        val retryPolicy = ExponentialBackoffRetry(3000, 10)

        //val retryPolicy = RetryForever(10000)

        val client = CuratorFrameworkFactory.builder().connectString(config.zkServer)
            .connectionTimeoutMs(TimeUnit.SECONDS.toMillis(30).toInt())
            .sessionTimeoutMs(TimeUnit.SECONDS.toMillis(60).toInt()) //60 秒节点超时
            .retryPolicy(retryPolicy).namespace("distributed_locks").build()

        client.connectionStateListenable.addListener(this.listener)
        client.start()
        return client
    }

    override fun acquire(name: String, scope: LockScope) {
        throwIfInvalidName(name)
        val model = getLockModel(name, scope)
        model.lock.acquire()
    }

    override fun tryAcquire(name: String, waitTimeout: Duration, scope: LockScope): Boolean {
        throwIfInvalidName(name)
        val model = getLockModel(name, scope)
        try {
            return model.lock.acquire(waitTimeout.toMillis(), TimeUnit.MILLISECONDS)
        } catch (ex: Throwable) {
            logger.error("acquire distributed lock from zookeeper fault.", ex)
            ex.throwIfNecessary()
            return false
        }
    }

    override fun release(name: String, scope: LockScope) {
        throwIfInvalidName(name)
        val model = getLockModel(name, scope)
        try {
            return model.lock.release()
        } catch (ex: IllegalMonitorStateException) {

        } catch (ex: Throwable) {
            ex.throwIfNecessary()
            logger.warn("Release distributed lock fault", ex)
        }
    }

    override fun close() {
        this.locks.values.toTypedArray().forEach {
            it.release()
        }
    }


    private fun getLockModel(name: String, scope: LockScope): LockModel {
        return locks.getOrPut(name) {
            if (client == null) synchronized(clientSync) {
                if (client == null) {
                    client = createClient(config)
                }
            }

            val lockName = if (scope == LockScope.Instance) "/instance/$applicationName/$name" else "/global/$name"
            val mutex = InterProcessMutex(client, lockName)
            LockModel(mutex, client!!)
        }
    }

    private class LockModel(val lock: InterProcessLock, val client: CuratorFramework) {
        fun release() {
            try {
                lock.release()
            } catch (ex: Throwable) {
                ex.throwIfNecessary()
            }
        }

    }
}