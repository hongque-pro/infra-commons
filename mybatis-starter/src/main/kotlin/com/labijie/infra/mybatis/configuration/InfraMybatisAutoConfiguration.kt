package com.labijie.infra.mybatis.configuration

import com.github.pagehelper.PageInterceptor
import com.labijie.infra.mybatis.IPageHelperCustomizer
import org.apache.ibatis.session.SqlSessionFactory
import org.mybatis.spring.SqlSessionFactoryBean
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*
import javax.sql.DataSource

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-12-27
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(MybatisAutoConfiguration::class)
@EnableConfigurationProperties(MybatisPageHelperProperties::class)
class InfraMybatisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(PageInterceptor::class)
    fun pageInterceptor(config: MybatisPageHelperProperties, customizers: ObjectProvider<IPageHelperCustomizer>): PageInterceptor {
        val properties = Properties()
        if(!config.helperDialect.isBlank()){
            properties.setProperty("helperDialect", config.helperDialect)
        }
        properties.setProperty("autoRuntimeDialect", config.autoRuntimeDialect.toString())

        customizers.orderedStream().forEach {
            it.configure(properties)
        }

        val interceptor = PageInterceptor()
        interceptor.setProperties(properties)
        return interceptor
    }
}