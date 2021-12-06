package com.labijie.infra.commons.snowflake.configuration

import com.labijie.infra.IIdGenerator
import com.labijie.infra.commons.snowflake.*
import com.labijie.infra.commons.snowflake.jdbc.SnowflakeSlotTable
import com.labijie.infra.commons.snowflake.providers.JdbcSlotProvider
import com.labijie.infra.commons.snowflake.providers.RedisSlotProvider
import com.labijie.infra.commons.snowflake.providers.StaticSlotProvider
import com.labijie.infra.commons.snowflake.providers.ZookeeperSlotProvider
import com.labijie.infra.orm.annotation.TableScan
import com.labijie.infra.orm.configuration.InfraExposedAutoConfiguration
import com.labijie.infra.orm.configuration.TableDefinitionPostProcessor
import com.labijie.infra.spring.configuration.NetworkConfig
import com.labijie.infra.utils.logger
import org.jetbrains.exposed.spring.SpringTransactionManager
import org.springframework.beans.factory.BeanCreationException
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.env.Environment
import org.springframework.transaction.support.TransactionTemplate

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-12
 */
@AutoConfigureAfter(Environment::class)
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SnowflakeProperties::class)
class SnowflakeAutoConfiguration {

    @ConditionalOnMissingBean(ISlotProvider::class)
    @ConditionalOnProperty(prefix = "infra.snowflake", name = ["provider"], havingValue = "static", matchIfMissing = false)
    @Bean
    fun staticSlotProvider(snowflakeConfig: SnowflakeProperties): StaticSlotProvider {
        return StaticSlotProvider(snowflakeConfig.static.slot)
    }

    @ConditionalOnMissingBean(ISlotProvider::class)
    @ConditionalOnProperty(prefix = "infra.snowflake", name = ["provider"], havingValue = "redis", matchIfMissing = false)
    @Bean
    fun redisSlotProvider(networkConfig: NetworkConfig, environment: Environment, snowflakeConfig: SnowflakeProperties): RedisSlotProvider {
        return RedisSlotProvider(environment, networkConfig, snowflakeConfig)
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(ISlotProvider::class)
    @ConditionalOnClass(name=["com.labijie.infra.orm.configuration.InfraExposedAutoConfiguration"])
    @ConditionalOnProperty(prefix = "infra.snowflake", name = ["provider"], havingValue = "jdbc", matchIfMissing = false)
    class JdbcSlotProviderAutoConfiguration {

        @Configuration(proxyBeanMethods = false)
        @TableScan(basePackageClasses = [SnowflakeSlotTable::class])
        class SnowflakeTableScanConfiguration

        @Bean
        fun jdbcSlotProvider(transactionTemplate: TransactionTemplate, networkConfig: NetworkConfig, environment: Environment, snowflakeConfig: SnowflakeProperties): JdbcSlotProvider {
            return JdbcSlotProvider(snowflakeConfig, networkConfig, transactionTemplate)
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "infra.snowflake", name = ["provider"], havingValue = "jdbc", matchIfMissing = false)
    @ConditionalOnMissingBean(JdbcSlotProvider::class)
    class ExposedStartNotFoundConfiguration : InitializingBean {
        override fun afterPropertiesSet() {
            throw BeanCreationException("jdbcSlotProvider", "Not found SpringTransactionManager bean in context, please put 'com.labijie.orm:exposed-starter' package in classpath.")
        }
    }

    @ConditionalOnMissingBean(ISlotProvider::class)
    @ConditionalOnProperty(prefix = "infra.snowflake", name = ["provider"], havingValue = "zookeeper", matchIfMissing = false)
    @Bean
    fun zookeeperSlotProvider(networkConfig: NetworkConfig, environment: Environment, snowflakeConfig: SnowflakeProperties): ISlotProvider {
        return ZookeeperSlotProvider(environment, networkConfig, snowflakeConfig)
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
    fun snowflakeIdGenerator(slotProviderFactory: ISlotProviderFactory, config:SnowflakeProperties): IIdGenerator {
        return SnowflakeIdGenerator(config, slotProviderFactory)
    }

    @Bean
    fun snowflakeRunner(): CommandLineRunner {
        return SnowflakeRunner()
    }
}