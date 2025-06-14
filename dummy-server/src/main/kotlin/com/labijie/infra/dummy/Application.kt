package com.labijie.infra.dummy

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Import

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-09-25
 */
@SpringBootApplication
@Import(AopTester::class)
class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}