package com.labijie.infra.mybatis

import java.util.*

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-05-17
 */
interface IPageHelperCustomizer{
    fun configure(pageInterceptor: Properties)
}