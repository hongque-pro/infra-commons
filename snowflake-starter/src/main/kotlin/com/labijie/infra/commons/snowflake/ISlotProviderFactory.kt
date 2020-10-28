package com.labijie.infra.commons.snowflake

import org.springframework.core.env.Environment

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-27
 */
interface ISlotProviderFactory {
    fun createProvider(providerName:String) : ISlotProvider
}