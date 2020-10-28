package com.labijie.infra.dummy

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory
import org.springframework.stereotype.Component
import java.util.*

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-09-26
 */
//@Component
//class ServletContainerCustomizer : WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
//
//
//    override fun customize(factory: ConfigurableServletWebServerFactory) {
//        factory.setPort(Random().nextInt(30000) + 10000)
//    }
//}