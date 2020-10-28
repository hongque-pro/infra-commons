package com.labijie.infra.distribution.configuration

import com.labijie.infra.distribution.DistributionRunner
import com.labijie.infra.distribution.IDistributedLock
import com.labijie.infra.distribution.annotation.DistributedSynchronizedAspect
import com.labijie.infra.distribution.impl.ZookeeperDistributedLock
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
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
@Configuration
@EnableConfigurationProperties(DistributedLockConfig::class)
class DistributionAutoConfiguration {

    @ConditionalOnMissingBean(IDistributedLock::class)
    @Bean
    fun zookeeperDistributedLock(config: DistributedLockConfig, environment: Environment): IDistributedLock {
        return ZookeeperDistributedLock(environment, config)
    }

    @Bean
    fun distributionRunner(lockConfig: IDistributedLock): CommandLineRunner {
        return DistributionRunner(lockConfig)
    }

    @Bean
    fun distributedSynchronizedAspect(lock:IDistributedLock): DistributedSynchronizedAspect {
        return DistributedSynchronizedAspect(lock)
    }
}