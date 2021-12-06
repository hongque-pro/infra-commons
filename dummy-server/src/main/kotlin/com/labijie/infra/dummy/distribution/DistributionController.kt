package com.labijie.infra.dummy.distribution

import com.labijie.infra.spring.configuration.NetworkConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-09-26
 */
@RestController
class DistributionController {
    @Autowired
    lateinit var network: NetworkConfig

    @GetMapping("/sync")
    fun sync(): String {

        return "OK! (${network.getIPAddress()})"
    }
}