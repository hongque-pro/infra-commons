package com.labijie.infra.configuration

import com.labijie.infra.IIdGenerator
import com.labijie.infra.orm.annotation.TableScan
import com.labijie.infra.snowflake.*
import com.labijie.infra.snowflake.jdbc.SnowflakeSlotTable
import com.labijie.infra.snowflake.providers.JdbcSlotProvider
import com.labijie.infra.snowflake.providers.RedisSlotProvider
import com.labijie.infra.snowflake.providers.StaticSlotProvider
import com.labijie.infra.snowflake.providers.ZookeeperSlotProvider
import org.springframework.beans.factory.BeanCreationException
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
@ConditionalOnMissingBean(IIdGenerator::class)
class SnowflakeAutoConfiguration {

    @ConditionalOnMissingBean(ISlotProvider::class)
    @ConditionalOnProperty(prefix = "infra.snowflake", name = ["provider"], havingValue = "static", matchIfMissing = true)
    @Bean
    fun staticSlotProvider(snowflakeConfig: SnowflakeProperties): StaticSlotProvider {
        return StaticSlotProvider(snowflakeConfig.static.slot)
    }

    @ConditionalOnMissingBean(ISlotProvider::class)
    @ConditionalOnProperty(prefix = "infra.snowflake", name = ["provider"], havingValue = "redis", matchIfMissing = false)
    @Bean
    fun redisSlotProvider(commonsProperties: com.labijie.infra.CommonsProperties, environment: Environment, snowflakeConfig: SnowflakeProperties): RedisSlotProvider {
        return RedisSlotProvider(environment, commonsProperties, snowflakeConfig)
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
        fun jdbcSlotProvider(transactionTemplate: TransactionTemplate, commonsProperties: com.labijie.infra.CommonsProperties, environment: Environment, snowflakeProperties: SnowflakeProperties): JdbcSlotProvider {
            return JdbcSlotProvider(snowflakeProperties, commonsProperties, transactionTemplate)
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "infra.snowflake", name = ["provider"], havingValue = "jdbc", matchIfMissing = false)
    @ConditionalOnMissingBean(JdbcSlotProvider::class)
    class ExposedStartNotFoundConfiguration : InitializingBean {
        override fun afterPropertiesSet() {
            throw BeanCreationException("jdbcSlotProvider", "Not found SpringTransactionManager bean in context, please put 'com.labijie.orm:exposed-springboot-starter' package in classpath.")
        }
    }

    @ConditionalOnMissingBean(ISlotProvider::class)
    @ConditionalOnProperty(prefix = "infra.snowflake", name = ["provider"], havingValue = "zookeeper", matchIfMissing = false)
    @Bean
    fun zookeeperSlotProvider(commonsProperties: com.labijie.infra.CommonsProperties, environment: Environment, snowflakeConfig: SnowflakeProperties): ISlotProvider {
        return ZookeeperSlotProvider(environment, commonsProperties, snowflakeConfig)
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
    fun snowflakeIdGenerator(slotProviderFactory: ISlotProviderFactory, config: SnowflakeProperties): IIdGenerator {
        return SnowflakeIdGenerator(config, slotProviderFactory)
    }

    @Bean
    fun snowflakeRunner(): CommandLineRunner {
        return SnowflakeRunner()
    }
}