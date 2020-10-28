package com.labijie.infra.mybatis.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-06-19
 */
@ConfigurationProperties("infra.mybatis.page")
data class MybatisPageHelperProperties(
        var helperDialect:String = "",
        var autoRuntimeDialect:Boolean = false) {
}