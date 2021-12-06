package com.labijie.infra.spring.configuration

import com.labijie.infra.InfrastructureException
import com.labijie.infra.utils.findIpAddress
import com.labijie.infra.utils.ifNullOrBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.env.Environment
import java.util.concurrent.ConcurrentHashMap

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-11-13
 */
@ConfigurationProperties("infra")
class NetworkConfig constructor(
        private val env: Environment?
){
    companion object {
        const val DEFAULT_NET_NAME = "__default"
        const val SPRING_CLOUD_NETWORK_CONFIG = "spring.cloud.inetutils.preferred-networks"
    }


    var networks = mutableMapOf<String, String>()

    private val ipAddresses: ConcurrentHashMap<String, String> = ConcurrentHashMap<String, String>()

    @JvmOverloads
    fun getIPAddress(networkName: String = DEFAULT_NET_NAME, throwIfNotFound: Boolean = false): String {
        val name = networkName.ifNullOrBlank(DEFAULT_NET_NAME)!!.trim()

        val isDefault = networkName == DEFAULT_NET_NAME
        val ip = ipAddresses.getOrPut(name) set@{
            val network = env?.getProperty(SPRING_CLOUD_NETWORK_CONFIG, "")
            if(!network.isNullOrBlank()) {
                val found = findIpAddress(network)
                if (!found.isNullOrBlank()) {
                    return found
                }
            }
            val mask = if(isDefault) this.networks.values.firstOrNull().ifNullOrBlank("*")!! else this.networks.getOrPut(name) { "*" }
            findIpAddress(mask).ifNullOrBlank("")!!
        }
        if(throwIfNotFound) {
            if (ip.isNullOrBlank()) {
                if (isDefault)
                    throw InfrastructureException("Cant found ip address that named '$name'.")
                else
                    throw InfrastructureException("Cant found ip address.")
            }
        }
        return ip
    }
}