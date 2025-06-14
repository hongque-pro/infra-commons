/**
 * THIS FILE IS PART OF HuanJing (huanjing.art) PROJECT
 * Copyright (c) 2023 huanjing.art
 * @author Huanjing Team
 */
package com.labijie.infra.configuration

import com.labijie.infra.snowflake.ISlotProvider
import com.labijie.infra.snowflake.providers.JdbcSlotProvider
import org.springframework.beans.factory.BeanCreationException
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@AutoConfigureAfter(SnowflakeAutoConfiguration::class)
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "infra.snowflake", name = ["provider"], havingValue = "jdbc", matchIfMissing = false)
@ConditionalOnMissingBean(ISlotProvider::class)
class JdbcSnowflakeCheckAutoConfiguration {

    @ConditionalOnMissingBean(JdbcSlotProvider::class)
    @Bean
    fun exposedNotFound(): JdbcSlotProvider {
        throw BeanCreationException("jdbcSlotProvider", "Infra-Orm package required for jdbc snowflake, please put 'com.labijie.orm:exposed-springboot-starter' package in classpath.")
    }
}