package com.labijie.infra.commons.snowflake.testing

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisException
import io.lettuce.core.RedisURI
import com.labijie.infra.commons.snowflake.SnowflakeException
import com.labijie.infra.commons.snowflake.configuration.SnowflakeConfig
import com.labijie.infra.commons.snowflake.providers.RedisSlotProvider
import com.labijie.infra.spring.configuration.NetworkConfig
import org.junit.jupiter.api.Assertions
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-09-10
 */
class RedisSlotProviderTester {
    private val slotProvider: RedisSlotProvider
    private val testRedisUrl = "redis://localhost:6379"

    init {
        slotProvider = createProvider()
    }

    private fun createProvider(): RedisSlotProvider {
        val snowConfig = SnowflakeConfig().apply {
            this.provider = "redis"
            this.redis.url = testRedisUrl
        }
        return RedisSlotProvider("test", true, NetworkConfig(null), snowConfig)
    }

    @BeforeTest
    fun setMAx() {
        RedisSlotProvider.maxSlotCount = 1024
        try {
            RedisClient.create().connect(RedisURI.create(testRedisUrl)).sync().flushall()
        } catch (e: RedisException) {
        }

    }

    @Test
    fun testConnect() {
        try {
            slotProvider.acquireSlot()
        } catch (e: SnowflakeException) {

        }
    }

    @Test
    fun testReuse() {
        try {
            val provider1 = this.createProvider()
            val provider2 = this.createProvider()
            val slot1 = provider1.acquireSlot()
            val slot2 = provider2.acquireSlot()
            Assertions.assertEquals(1, slot1)
            Assertions.assertEquals(2, slot2)

            provider1.close()
            Assertions.assertEquals(-1, provider1.currentSlot)
            val provider3 = this.createProvider()
            val slot3 = provider3.acquireSlot()
            Assertions.assertEquals(1, slot3)

        } catch (e: SnowflakeException) {
        }
    }

    @Test
    fun testFullSlot() {
        val testCount = 10
        RedisSlotProvider.maxSlotCount = 10
        val providers = mutableListOf<RedisSlotProvider>()

        (1..testCount + 1).forEach {
            val provider = createProvider().apply {
                providers.add(this)
            }
            if (it > testCount) {
                Assertions.assertThrows(SnowflakeException::class.java) {
                    provider.acquireSlot()
                }
            } else {
                provider.acquireSlot()
            }
        }

        println("assigned slot: ${providers.joinToString { it.currentSlot.toString() }}")
        providers.forEach {
            it.close()
        }
    }
}