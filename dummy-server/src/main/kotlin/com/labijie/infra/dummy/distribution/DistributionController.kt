package com.labijie.infra.dummy.distribution

import com.labijie.infra.dummy.AopTester
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-09-26
 */
@RestController
class TestController {

    @Autowired
    private lateinit var aopTester: AopTester

    @GetMapping("/test")
    fun test() {

        aopTester.test()
    }
}