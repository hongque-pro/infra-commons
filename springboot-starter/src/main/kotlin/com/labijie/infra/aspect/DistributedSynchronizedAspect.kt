/**
 * @author Anders Xiao
 * @date 2025-06-14
 */
package com.labijie.infra.aspect

import com.labijie.infra.annotation.DistributedSynchronized
import com.labijie.infra.distribution.IDistributedLock
import com.labijie.infra.utils.ifNullOrBlank
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.core.annotation.Order


@Aspect
@Order(1)
class DistributedSynchronizedAspect(private val distributedLock: IDistributedLock) {

    @Pointcut("@annotation(com.labijie.infra.annotation.DistributedSynchronized)")
    fun pointCut() {

    }

    @Around("pointCut()")
    fun around(joinPoint: ProceedingJoinPoint):Any? {

        val methodSig = joinPoint.signature as MethodSignature

        // 通过 AOP 获取注解（Spring 会 AOT 编译时处理好，不需反射）
        val annotation = methodSig.method.getDeclaredAnnotation(DistributedSynchronized::class.java)!!

        // 如果 lockName 没指定，就使用方法签名字符串
        val lockName = annotation.lockName.ifNullOrBlank { methodSig.declaringType.name + "." + methodSig.name }

        val scope = annotation.scope

        distributedLock.acquire(lockName, scope)
        try {
            return joinPoint.proceed(joinPoint.args ?: arrayOf())
        } finally {
            distributedLock.release(lockName, scope)
        }

    }

}