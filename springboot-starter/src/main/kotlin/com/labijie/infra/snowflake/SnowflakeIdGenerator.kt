package com.labijie.infra.snowflake

import com.labijie.infra.IIdGenerator
import com.labijie.infra.snowflake.SnowflakeProperties.Companion.DEFAULT_SNOWFLAKE_START
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter



/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-12
 */
class SnowflakeIdGenerator(snowflakeConfig: SnowflakeProperties, slotProviderFactory: ISlotProviderFactory) : IIdGenerator {

    private var slotId: Int = -1
    private var kernel: SnowflakeKernel? = null
    private val lazyLock: Any = Any()
    private val slotProvider: ISlotProvider = slotProviderFactory.createProvider(snowflakeConfig.provider)
    private val timestamp: Long

    companion object {
        private val logger by lazy {
            LoggerFactory.getLogger(SnowflakeIdGenerator::class.java)
        }
    }

    init {
        var start = snowflakeConfig.startTimestamp
        if(start == DEFAULT_SNOWFLAKE_START) {
            start = System.getenv("SNOW_FLAKE_START")?.toLongOrNull() ?: snowflakeConfig.startTimestamp
        }
        timestamp = start
        if(timestamp == DEFAULT_SNOWFLAKE_START) {
            val startDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(start), ZoneOffset.UTC)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            StringBuilder().appendLine("Snowflake seed is too old (start timestamp: ${startDateTime.format(formatter)})")
                .appendLine("Use one of below to solve this issue")
                .appendLine("   - Use environment variable (Recommend): export SNOW_FLAKE_START=<epoch millisecond>")
                .appendLine("   - Use command-line arguments: -Dinfra.snowflake.start-timestamp=<epoch millisecond>")
                .appendLine("   - Use application.yml: infra.snowflake.start-timestamp=<epoch millisecond>")
                .toString().let {
                    logger.debug(it)
                }
        }
    }

    override fun newId(): Long {
        if (slotId == -1 || this.kernel == null) {
            synchronized(this.lazyLock) {
                if (slotId == -1) {
                    this.slotId = slotProvider.acquireSlot(true)!!
                    this.kernel = SnowflakeKernel(this.slotId.toLong(), timestamp)
                }
            }
        }
        return this.kernel!!.nextId()
    }
}