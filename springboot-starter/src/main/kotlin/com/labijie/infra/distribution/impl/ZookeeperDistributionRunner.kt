package com.labijie.infra.distribution.impl

import com.labijie.infra.distribution.IDistributedLock
import org.apache.curator.framework.state.ConnectionState
import org.apache.curator.framework.state.ConnectionStateListener
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import java.util.concurrent.TimeUnit

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-09-19
 */

class ZookeeperDistributionRunner(private val distributedLock: IDistributedLock) : CommandLineRunner {

    @Volatile
    private var stop: Boolean = false
    @Volatile
    private var isConnected = true
    @Volatile
    private var acquiring = false

    companion object {
        private val logger by lazy {
            LoggerFactory.getLogger("com.labijie.infra.distribution.impl.ZookeeperDistributionRunner")
        }
    }

    private val listener: ConnectionStateListener = ConnectionStateListener { _, newState ->
        val connected = newState == ConnectionState.RECONNECTED || newState == ConnectionState.CONNECTED || newState == ConnectionState.READ_ONLY
        if (this.isConnected != connected) {
            this.isConnected = connected
            if (!this.isConnected) {
                logger.warn("The connection to zookeeper for IDistributedLock has been disconnected.")
            } else {
                logger.debug("The connection to zookeeper was reconnected.")
                this.run() //如果正在请求，该操作无效，如果获取到锁，该操作将尝试重新获取锁
            }
        }

    }

    override fun run(vararg args: String?) {
        Runtime.getRuntime().addShutdownHook(Thread {
            this.stop = true
            if(DistributionInstance.status == InstanceStatus.Master){
                this.distributedLock.release(IDistributedLock.INSTANCE_LOCK_NAME)
            }
        })

        if (!this.acquiring) {
            this.acquiring = true
            val thread = Thread {

                val zkLock = this.distributedLock as? ZookeeperDistributedLock
                zkLock?.listeners?.add(this.listener)

                do {
                    this.distributedLock.release(IDistributedLock.INSTANCE_LOCK_NAME)
                    val locked = this.distributedLock.tryAcquire(IDistributedLock.INSTANCE_LOCK_NAME,
                        java.time.Duration.ofMillis(20)
                    )
                    if (locked) {
                        DistributionInstance.status = InstanceStatus.Master
                        this.acquiring = false
                        logger.debug("Get instance lock, switch to master.")
                        return@Thread
                    } else {
                        Thread.sleep(TimeUnit.SECONDS.toMillis(10))
                    }
                } while (!stop)
            }
            thread.name = "distributed-lock"
            thread.start()
        }
    }
}