package com.labijie.infra.snowflake

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-27
 */
class DefaultSlotProviderFactory() : ISlotProviderFactory, ApplicationContextAware {
    private lateinit var applicationContext:ApplicationContext

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    override fun createProvider(providerName: String): ISlotProvider {
        return this.applicationContext.getBean(ISlotProvider::class.java)
    }
}