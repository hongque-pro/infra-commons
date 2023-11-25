package com.labijie.infra.annotation

import com.labijie.infra.distribution.LockScope

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-09-25
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DistributedSynchronized(val lockName:String = "", val scope: LockScope = LockScope.Instance) {

}
