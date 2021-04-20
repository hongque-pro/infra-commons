package com.labijie.infra.mybatis.configuration

import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.context.annotation.Configuration

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-12-27
 */
@Configuration
@AutoConfigureBefore(MybatisAutoConfiguration::class)
class MybatisDynamicAutoConfiguration