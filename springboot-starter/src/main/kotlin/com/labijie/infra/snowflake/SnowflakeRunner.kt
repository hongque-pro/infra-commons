package com.labijie.infra.snowflake

import com.labijie.infra.IIdGenerator
import com.labijie.infra.utils.logger
import com.labijie.infra.utils.throwIfNecessary
import org.springframework.boot.CommandLineRunner
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.core.Ordered
import kotlin.system.exitProcess

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-12
 */
class SnowflakeRunner : CommandLineRunner, ApplicationContextAware, Ordered {
    override fun getOrder(): Int {
        return 0
    }

    private var applicationContext: ApplicationContext? = null

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    override fun run(vararg args: String?) {
        val snowflakeIdGenerator = applicationContext!!.getBean(IIdGenerator::class.java) as? SnowflakeIdGenerator
        if(snowflakeIdGenerator != null) {
            try {
                snowflakeIdGenerator.newId()
                logger.info("SnowflakeIdGenerator id generation has been tested successfully !")
            } catch (ex: Throwable) {
                this.logger.error("Error occurred when starting the ZookeeperSlotProvider.", ex)
                ex.throwIfNecessary()
                exitProcess(-9999)
            }
        }
    }
}