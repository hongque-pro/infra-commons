package com.labijie.infra.commons.snowflake.testing

import com.labijie.infra.commons.snowflake.SnowflakeException
import com.labijie.infra.commons.snowflake.providers.ZookeeperSlotProvider
import com.labijie.infra.commons.snowflake.configuration.SnowflakeProperties
import com.labijie.infra.spring.configuration.NetworkConfig
import com.labijie.infra.utils.ifNullOrBlank
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.lang.reflect.InvocationTargetException
import java.time.Duration
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.isAccessible

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-12
 */
class ZookeeperSlotProviderTester {

    companion object {
        const val TEST_ZK_SERVER = "localhost:2181"
    }

    @Test
    fun testErrorConnect() {
        createSlotProvider("8.8.8.8:2316").use {

            val method = ZookeeperSlotProvider::class.functions.find { m -> m.name == "createClient" }
            method!!.isAccessible = true
            val ex = Assertions.assertThrows(InvocationTargetException::class.java) {
                method.call(it, 3000L)
            }
            Assertions.assertNotNull(ex.cause as? SnowflakeException)
        }
    }

    @Test
    fun testReuseSlot() {
        val set = mutableSetOf<ZookeeperSlotProvider>()
        try {
            val provider1 = createSlotProvider()
            val provider2 = createSlotProvider()
            set.add(provider1)
            set.add(provider2)

            try {
                val slot1 = provider1.acquireSlot()

                Assertions.assertEquals(1, slot1)

                provider1.disconnect()

                val slot2 = provider2.acquireSlot()
                val excepted = if(provider2.isStarted) 2 else 1
                Assertions.assertEquals(excepted, slot2)
            } finally {
                provider1.disconnect()
                provider2.disconnect()
            }
        } catch (_: SnowflakeException) {

        }finally {
            set.forEach {
                it.disconnect()
            }
        }
    }

    @Test
    fun testFullSlot() {
        val set = mutableSetOf<ZookeeperSlotProvider>()
        val testCount = 10;
        try {
            (1..testCount + 1).forEach {
                val provider1 = createSlotProvider()
                provider1.maxSlotCount = testCount
                set.add(provider1)

                if (it == (testCount + 1) && provider1.isStarted) {
                    Assertions.assertThrows(SnowflakeException::class.java) {
                        provider1.acquireSlot()
                    }
                } else {
                    provider1.acquireSlot()
                }
            }
        } catch (_: SnowflakeException) {

        } finally {
            set.forEach {
                it.disconnect()
            }
        }
    }

    private fun createSlotProvider(
        server: String? = null,
        slotTimeout: Duration = Duration.ofSeconds(1)
    ): ZookeeperSlotProvider {
        val config = SnowflakeProperties().apply {
            scope = "dummy"
            zk.server = server.ifNullOrBlank(TEST_ZK_SERVER)!!
            zk.sessionTimeout = slotTimeout
            zk.connectTimeout = Duration.ofSeconds(2)
        }
        val networkConfig = NetworkConfig(null)
        return ZookeeperSlotProvider("infra-test-project", true, networkConfig, config)
    }
}