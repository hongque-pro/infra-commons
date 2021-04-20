package com.labijie.infra.kryo

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-02-19
 */
class Pooled<TValue> internal constructor(val instance:TValue, private val returnObject:()->Unit):AutoCloseable {
    override fun close() {
        this.returnObject()
    }
}