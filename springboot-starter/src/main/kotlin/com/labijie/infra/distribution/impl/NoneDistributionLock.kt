package com.labijie.infra.distribution.impl

import com.labijie.infra.distribution.IDistributedLock
import com.labijie.infra.distribution.LockScope
import org.apache.commons.logging.LogFactory
import java.time.Duration

/**
 * @author Anders Xiao
 * @date 2025/6/14
 */
class NoneDistributionLock : IDistributedLock {

    companion object {
        private val logger by lazy {
            LogFactory.getLog(NoneDistributionLock::class.java)
        }
    }

    override fun acquire(name: String, scope: LockScope) {
        logger.warn("No distribution lock: acquired for name $name with scope $scope")
    }

    override fun tryAcquire(name: String, waitTimeout: Duration, scope: LockScope): Boolean {
        logger.warn("No distribution lock: acquired for name $name with scope $scope, wait timeout: ${waitTimeout.toSeconds()} seconds")
        return true
    }

    override fun release(name: String, scope: LockScope) {

    }
}