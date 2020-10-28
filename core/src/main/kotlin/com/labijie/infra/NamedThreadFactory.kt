package com.labijie.infra

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-02-16
 */
class NamedThreadFactory(private val isDaemon:Boolean, private val nameFactory:(Int)->String) : ThreadFactory {
    private val index = AtomicInteger(0)
    override fun newThread(r: Runnable): Thread {
        return Thread(r).also {
            it.name = nameFactory(index.incrementAndGet())
            it.isDaemon = isDaemon
        }
    }
}