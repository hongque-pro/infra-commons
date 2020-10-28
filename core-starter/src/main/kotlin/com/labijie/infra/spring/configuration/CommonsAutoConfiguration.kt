package com.labijie.infra.spring.configuration

import com.labijie.infra.IIdGenerator
import com.labijie.infra.impl.DebugIdGenerator
import com.labijie.infra.security.Rfc6238TokenService
import com.labijie.infra.security.Rfc6238TokenServiceProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-12
 */
@Configuration
@EnableConfigurationProperties(_root_ide_package_.com.labijie.infra.spring.configuration.NetworkConfig::class)
class CommonsAutoConfiguration {

    @ConditionalOnMissingBean(IIdGenerator::class)
    @Bean
    fun debugIdGenerator(): IIdGenerator = DebugIdGenerator()

    @ConfigurationProperties("infra.security.rfc6238")
    @Bean
    fun rfc6238TokenServiceProperties(): Rfc6238TokenServiceProperties {
        return Rfc6238TokenServiceProperties()
    }

    @Bean
    fun rfc6238TokenService(): Rfc6238TokenService {
        return Rfc6238TokenService()
    }
}