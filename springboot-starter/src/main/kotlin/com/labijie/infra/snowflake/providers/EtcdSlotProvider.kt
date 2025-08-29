import com.labijie.infra.CommonsProperties
import com.labijie.infra.snowflake.HostIdentifier
import com.labijie.infra.snowflake.ISlotProvider
import com.labijie.infra.snowflake.SnowflakeBitsConfig
import com.labijie.infra.snowflake.SnowflakeException
import com.labijie.infra.snowflake.SnowflakeProperties
import io.etcd.jetcd.ByteSequence
import io.etcd.jetcd.Client
import io.etcd.jetcd.lease.LeaseGrantResponse
import io.etcd.jetcd.lease.LeaseKeepAliveResponse
import io.etcd.jetcd.op.Cmp
import io.etcd.jetcd.op.Cmp.Op
import io.etcd.jetcd.op.CmpTarget
import io.etcd.jetcd.options.PutOption
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class EtcdSlotProvider(
    private val properties: CommonsProperties,
    private val config: SnowflakeProperties
) : ISlotProvider {

    private var maxSlotCount = SnowflakeBitsConfig.DEFAULT_MACHINES_PER_CENTER

    private val client: Client by lazy {
        val endpoints = config.etcd.endpoints.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toTypedArray()
        Client.builder()
            .endpoints(*endpoints)
            .build()
    }


    private val scope by lazy {
        val s = config.fixedScope("/")

        s.apply {
            if(s.isBlank()) {
                throw SnowflakeException("Snowflake scope cannot be blank")
            }
        }
    }

    companion object {
        private val logger by lazy {
            LoggerFactory.getLogger(EtcdSlotProvider::class.java)
        }
    }

    private val leaseClient = client.leaseClient
    private val kvClient = client.kvClient

    private var leaseId: Long? = null
    private var acquiredSlot: Int? = null
    private val isReleased = AtomicBoolean(false)

    private val identifier: String by lazy {
        when (config.etcd.identifier) {
            HostIdentifier.UUID -> UUID.randomUUID().toString()
            HostIdentifier.HostName ->
                System.getenv("HOSTNAME")
                    ?: System.getenv("COMPUTERNAME")
                    ?: properties.getIPAddress()
        }
    }

    fun safeGrantTTL(retries: Int = 3): LeaseGrantResponse {
        var lastError: Exception? = null
        val ttl = config.etcd.ttl.toSeconds().coerceAtLeast(10)
        repeat(retries) { attempt ->
            try {
                return leaseClient.grant(ttl, config.etcd.connectTimeout.toSeconds().coerceAtLeast(3), TimeUnit.SECONDS).get()
            } catch (ex: Exception) {
                lastError = ex
                logger.error("etcd lease grant attempt ${attempt + 1} failed: ${ex.message}")
                Thread.sleep(1000L * (attempt + 1)) // 简单指数退避
            }
        }
        throw SnowflakeException("Failed to grant lease from etcd after $retries retries", lastError)
    }

    private fun getEtcdKey(slot: Int): String {
        return "/${scope}/snowflake_slots/$slot"
    }

    override fun acquireSlot(throwIfNoneSlot: Boolean): Int? {
        val lease = safeGrantTTL()

        leaseId = lease.id
        val value = ByteSequence.from(identifier, StandardCharsets.UTF_8)

        for (slot in 0 until 1024) {
            val path = getEtcdKey(slot)
            val key = ByteSequence.from(path, StandardCharsets.UTF_8)
            val condition = Cmp(key, Op.EQUAL, CmpTarget.version(0))
            val putOption = PutOption.builder().withLeaseId(lease.id).build()

            val txn = kvClient.txn()
                .If(condition)
                .Then(io.etcd.jetcd.op.Op.put(key, value, putOption))
                .commit()
                .get()

            if (txn.isSucceeded) {
                logger.info("Snowflake slot got, slot: '$path'.")
                acquiredSlot = slot
                startKeepAliveWithStreamObserver(lease.id)
                addShutdownHook()
                return slot
            }
        }

        if (throwIfNoneSlot) {
            throw SnowflakeException("There is no available slot for snowflake (etc slot provider) .")
        }
        return null
    }

    override fun setMaxSlots(maxSlots: Int) {
        this.maxSlotCount = maxSlots
    }

    private fun startKeepAliveWithStreamObserver(leaseId: Long) {
        leaseClient.keepAlive(leaseId, object : StreamObserver<LeaseKeepAliveResponse> {
            override fun onNext(resp: LeaseKeepAliveResponse) {
                if(logger.isDebugEnabled) {
                    logger.debug("Snowflake slot (etcd) keepAlive OK: lease=${resp.id}, TTL=${resp.ttl}")
                }
            }

            override fun onError(t: Throwable) {
                logger.error("Snowflake slot (etcd) keepAlive error: ", t)
            }

            override fun onCompleted() {

            }
        })
    }

    private fun addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(Thread {
            releaseSlot()
        })
    }

    private fun releaseSlot() {
        if (isReleased.getAndSet(true)) return

        acquiredSlot?.let {
            val key = ByteSequence.from(getEtcdKey(it), StandardCharsets.UTF_8)
            try {
                kvClient.delete(key).get()
                logger.info("Etcd snowflake slot $it released.")
            } catch (ex: Throwable) {
                logger.error("Etcd snowflake slot failed.", ex)
            }
        }

        leaseId?.let {
            try {
                leaseClient.revoke(it).get()
                logger.info("Etc snowflake lease released, leaseId=$it")
            } catch (ex: Throwable) {
                logger.error("Release etc snowflake lease failed.", ex)
            }
        }
    }
}
