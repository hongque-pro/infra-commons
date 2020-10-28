package com.labijie.infra.distribution

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-09-19
 */
interface IDistributedLock : AutoCloseable {
    companion object {
        const val INSTANCE_LOCK_NAME = "__instance_lock";
    }

    fun acquire(name: String, scope: LockScope = LockScope.Instance)

    fun acquire(name: String, timeoutMilliseconds: Long, scope: LockScope = LockScope.Instance): Boolean

    fun release(name: String, scope:LockScope = LockScope.Instance): Unit
}