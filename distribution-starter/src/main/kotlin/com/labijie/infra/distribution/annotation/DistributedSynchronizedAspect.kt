package com.labijie.infra.distribution.annotation

import com.labijie.infra.distribution.IDistributedLock
import com.labijie.infra.distribution.LockScope
import com.labijie.infra.utils.ifNullOrBlank
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.core.annotation.Order
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.core.annotation.AnnotatedElementUtils
import javax.rmi.CORBA.Util


/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-09-25
 */
@Aspect
@Order(1)
class DistributedSynchronizedAspect(private val distributedLock: IDistributedLock) {
    @Pointcut("@annotation(com.labijie.infra.distribution.annotation.DistributedSynchronized)")
    private fun pointCut() {}

    @Around("pointCut()")
    fun around(joinPoint:JoinPoint):Any {
        val point = joinPoint as ProceedingJoinPoint
        val method = (point.signature as MethodSignature).method
        val anno = AnnotatedElementUtils.findMergedAnnotation(method, DistributedSynchronized::class.java)
        //val anno = method.getAnnotation(DistributedSynchronized::class.java)
        val syncName = (anno?.lockName).ifNullOrBlank("${method.declaringClass.name}.${method.name}")!!

        this.distributedLock.acquire(syncName, LockScope.Instance)
        try {
            return point.proceed(joinPoint.args)
        }
        finally {
            this.distributedLock.release(syncName, LockScope.Instance)
        }
    }

}