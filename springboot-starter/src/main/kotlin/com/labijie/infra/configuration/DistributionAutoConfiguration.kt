package com.labijie.infra.configuration

import com.labijie.infra.aspect.DistributedSynchronizedAspect
import com.labijie.infra.distribution.DistributedProperties
import com.labijie.infra.distribution.impl.ZookeeperDistributionRunner
import com.labijie.infra.distribution.IDistributedLock
import com.labijie.infra.distribution.impl.ZookeeperDistributedLock
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.core.env.Environment

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-09-19
 */
@EnableAspectJAutoProxy
@AutoConfigureAfter(Environment::class)
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DistributedProperties::class)
class DistributionAutoConfiguration {

    @Bean
    fun distributedSynchronizedAspect(lock:IDistributedLock): DistributedSynchronizedAspect {
        return DistributedSynchronizedAspect(lock)
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "infra.distribution", name = ["provider"], havingValue = "zookeeper", matchIfMissing = false)
    protected class ZookeeperDistributionImplAutoConfiguration {
        @ConditionalOnMissingBean(IDistributedLock::class)
        @Bean
        fun zookeeperDistributedLock(config: DistributedProperties, environment: Environment): IDistributedLock {
            return ZookeeperDistributedLock(environment, config)
        }

        @Bean
        @ConditionalOnMissingBean(ZookeeperDistributionRunner::class)
        fun distributionRunner(lockConfig: IDistributedLock): CommandLineRunner {
            return ZookeeperDistributionRunner(lockConfig)
        }
    }
}