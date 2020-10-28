package com.labijie.infra.commons.snowflake

import com.labijie.infra.IIdGenerator
import com.labijie.infra.commons.snowflake.configuration.SnowflakeConfig
import com.labijie.infra.utils.Constants
import com.labijie.infra.utils.logger
import org.springframework.core.env.Environment

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-12
 */
class SnowflakeIdGenerator(snowflakeConfig: SnowflakeConfig, slotProviderFactory: ISlotProviderFactory) : IIdGenerator {

    private var slotId:Int = -1
    private var kernel:SnowflakeKernel? = null
    private val lazyLock:Any = Any()
    private val slotProvider:ISlotProvider = slotProviderFactory.createProvider(snowflakeConfig.provider)

    override fun newId(): Long {
        if(slotId == -1 || this.kernel == null){
            synchronized(this.lazyLock){
                if(slotId == -1){
                    this.slotId = slotProvider.acquireSlot(true)!!
                    this.kernel = SnowflakeKernel(this.slotId.toLong())
                }
            }
        }
        return this.kernel!!.nextId()
    }
}