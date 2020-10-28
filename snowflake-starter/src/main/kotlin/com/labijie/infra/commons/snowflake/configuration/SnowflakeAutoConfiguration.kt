package com.labijie.infra.commons.snowflake.configuration

import com.labijie.infra.IIdGenerator
import com.labijie.infra.commons.snowflake.*
import com.labijie.infra.commons.snowflake.providers.RedisSlotProvider
import com.labijie.infra.commons.snowflake.providers.StaticSlotProvider
import com.labijie.infra.commons.snowflake.providers.ZookeeperSlotProvider
import com.labijie.infra.spring.configuration.NetworkConfig
import com.labijie.infra.spring.configuration.getApplicationName
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-12
 */
@AutoConfigureAfter(Environment::class)
@Configuration
@EnableConfigurationProperties(SnowflakeConfig::class)
class SnowflakeAutoConfiguration {

    @ConditionalOnProperty(prefix = "infra.snowflake", name = ["provider"], havingValue = "zookeeper", matchIfMissing = true)
    @Bean
    fun zookeeperSlotProvider(networkConfig: NetworkConfig, environment: Environment, snowflakeConfig: SnowflakeConfig): ISlotProvider {
        return ZookeeperSlotProvider(environment, networkConfig, snowflakeConfig)
    }

    @ConditionalOnProperty(prefix = "infra.snowflake", name = ["provider"], havingValue = "static", matchIfMissing = false)
    @Bean
    fun staticSlotProvider(snowflakeConfig: SnowflakeConfig): ISlotProvider {
        return StaticSlotProvider(snowflakeConfig.static.slot)
    }

    @ConditionalOnProperty(prefix = "infra.snowflake", name = ["provider"], havingValue = "redis", matchIfMissing = false)
    @Bean
    fun redisSlotProvider(networkConfig: NetworkConfig, environment: Environment, snowflakeConfig: SnowflakeConfig): ISlotProvider {
        return RedisSlotProvider(environment, networkConfig, snowflakeConfig)
    }


    @ConditionalOnMissingBean(ISlotProviderFactory::class)
    @Bean
    fun defaultSlotProviderFactory(): ISlotProviderFactory {
        return DefaultSlotProviderFactory()
    }



//    @Bean
//    fun etcdSlotProvider(environment: Environment, snowflakeConfig: SnowflakeConfig): EtcdSlotProvider {
//        val applicationName:String? = environment.getProperty("spring.application.name")
//        if(applicationName.isNullOrBlank()){
//            throw SnowflakeException("Snowflake component require the spring application name.")
//        }
//        return EtcdConfig(applicationName!!, environment, snowflakeConfig)
//    }

    @Bean
    fun snowflakeIdGenerator(slotProviderFactory: ISlotProviderFactory, config:SnowflakeConfig): IIdGenerator {
        return SnowflakeIdGenerator(config, slotProviderFactory)
    }

    @Bean
    fun snowflakeRunner(): CommandLineRunner {
        return SnowflakeRunner()
    }
}