package com.labijie.infra.distribution

import java.time.Duration

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-09-19
 */
interface IDistributedLock {
    companion object {
        const val INSTANCE_LOCK_NAME = "__instance_lock";
    }

    fun acquire(name: String, scope: LockScope = LockScope.Instance)

    fun tryAcquire(name: String, waitTimeout: Duration, scope: LockScope = LockScope.Instance): Boolean

    fun release(name: String, scope: LockScope = LockScope.Instance): Unit
}