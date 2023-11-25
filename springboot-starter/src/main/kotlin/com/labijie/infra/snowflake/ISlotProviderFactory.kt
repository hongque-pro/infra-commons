package com.labijie.infra.snowflake

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-27
 */
interface ISlotProviderFactory {
    fun createProvider(providerName:String) : ISlotProvider
}