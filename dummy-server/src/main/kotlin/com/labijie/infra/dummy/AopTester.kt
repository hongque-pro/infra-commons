package com.labijie.infra.dummy

import com.labijie.infra.annotation.DistributedSynchronized
import org.springframework.stereotype.Component

/**
 * @author Anders Xiao
 * @date 2025/6/14
 */
@Component
class AopTester {

    @DistributedSynchronized("")
    fun test() {
        println("Aop test")
    }
}