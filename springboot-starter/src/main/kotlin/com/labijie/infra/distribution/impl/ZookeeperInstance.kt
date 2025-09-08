package com.labijie.infra.distribution.impl

import com.labijie.infra.utils.throwIfNecessary
import org.slf4j.LoggerFactory

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-09-19
 */
object DistributionInstance {

    private val logger by lazy {
        LoggerFactory.getLogger("com.labijie.infra.distribution.impl.DistributionInstance")
    }

    private val listeners: MutableSet<()->Unit> = mutableSetOf()

    var status: InstanceStatus = InstanceStatus.Standby
        internal set(value){
            if(field != value){
                field = value
                listeners.forEach{
                    try {
                        it()
                    }catch (ex:Throwable){
                        ex.throwIfNecessary()
                        logger.error("An error occurred while processing an instance state change callback.", ex)
                    }
                }
            }
        }

    fun registerStatusCallback(callback:()->Unit){
        listeners.add(callback)
    }
}

enum class InstanceStatus {
    Master, Standby
}