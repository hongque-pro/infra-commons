package com.labijie.infra.configuration

import com.labijie.infra.CommonsProperties
import com.labijie.infra.security.IRfc6238TokenService
import com.labijie.infra.security.Rfc6238TokenService
import com.labijie.infra.security.Rfc6238TokenServiceProperties
import org.springframework.boot.autoconfigure.AutoConfigureOrder
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-12
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CommonsProperties::class)
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
class CommonsAutoConfiguration {

    @ConfigurationProperties("infra.security.rfc6238")
    @Bean
    fun rfc6238TokenServiceProperties(): Rfc6238TokenServiceProperties {
        return Rfc6238TokenServiceProperties()
    }

    @Bean
    @ConditionalOnMissingBean(IRfc6238TokenService::class)
    fun rfc6238TokenService(): Rfc6238TokenService {
        return Rfc6238TokenService()
    }
}