/**
 * @author Anders Xiao
 * @date 2025/8/29
 */


package com.labijie.infra.snowflake.providers

import com.labijie.infra.snowflake.ISlotProvider
import com.labijie.infra.snowflake.SnowflakeException


/**
 * Kubernetes StatefulSet-based SlotProvider
 *
 * This provider derives the slot from the pod's stable ordinal suffix,
 * guaranteed by StatefulSet.
 *
 * Features:
 * - Uses the pod name suffix (e.g., "-0", "-1") as the slot number.
 * - No lease, lock, or renewal logic needed.
 * - Slot is stable and unique for each pod.
 * - Suitable for fixed-replica StatefulSets.
 */
class KubernetesStatefulSlotProvider(
    private var maxSlots: Int = Int.MAX_VALUE
) : ISlotProvider {

    // Retrieve the pod name from the environment variable
    private val podName: String = System.getenv("HOSTNAME")
        ?: throw SnowflakeException("HOSTNAME environment variable not found, cannot determine pod slot")

    // Compute slot from pod name suffix
    private val slot: Int by lazy {
        val suffix = podName.substringAfterLast('-', "")
        if (suffix.isBlank() || !suffix.all { it.isDigit() }) {
            throw SnowflakeException("Pod name [$podName] is not in StatefulSet format (must end with -N)")
        }
        val s = suffix.toInt()
        if (s >= maxSlots) {
            throw SnowflakeException("Pod slot $s exceeds maxSlots=$maxSlots (pod=$podName)")
        }
        s
    }

    /**
     * Acquire the slot assigned to this pod.
     *
     * @param throwIfNoneSlot Whether to throw an exception if the slot is invalid
     * @return the slot number, or null if invalid and throwIfNoneSlot=false
     * @throws SnowflakeException if the slot is invalid and throwIfNoneSlot=true
     */
    @Throws(SnowflakeException::class)
    override fun acquireSlot(throwIfNoneSlot: Boolean): Int? {
        return if (slot < maxSlots) {
            slot
        } else {
            if (throwIfNoneSlot) throw SnowflakeException("No valid slot for pod $podName")
            else null
        }
    }

    /**
     * Set the maximum allowed slots.
     *
     * @param maxSlots the maximum number of slots
     */
    override fun setMaxSlots(maxSlots: Int) {
        this.maxSlots = maxSlots
    }
}