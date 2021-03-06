package com.labijie.infra.impl

import com.labijie.infra.IIdGenerator
import com.labijie.infra.utils.logger
import java.util.concurrent.atomic.AtomicLong

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-12
 */
class DebugIdGenerator : IIdGenerator {
    val seed = AtomicLong(System.currentTimeMillis())

    @Volatile
    private var logged = false

    override fun newId(): Long {
        if(logged && logger.isWarnEnabled) {
            logged = true
            this.logger.warn("DebugIdGenerator for unit testing only, do not use in a production environment.")
        }
        return seed.incrementAndGet()
    }
}