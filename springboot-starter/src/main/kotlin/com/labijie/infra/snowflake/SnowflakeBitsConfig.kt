/**
 * @author Anders Xiao
 * @date 2025/8/29
 */
package com.labijie.infra.snowflake



/**
 * Snowflake ID bit configuration.
 *
 * Memory layout of the Snowflake ID:
 *
 * Bit:  63                     22 21 20 19 18 17 16 15 14 13 12 11           0
 * +-------------------------------+--------+--------+----------------+
 * |          timestamp            |datacenter|machineId|  sequence     |
 * +-------------------------------+--------+--------+----------------+
 *
 * Default maximum values (using default constructor values):
 * - Sequence: 2^12 - 1 = 4095 (per millisecond per machine)
 * - Machine ID: 2^7 - 1 = 127 → 128 machines per data center
 * - Data center ID: 2^3 - 1 = 7 → 8 data centers
 * - Nodes per data center: 128
 * - Total nodes: 8 data centers × 128 machines = 1024
 *
 * @property sequenceBits Number of bits used for the sequence.
 * @property machineBits Number of bits used for the machine ID.
 * @property datacenterBits Number of bits used for the data center ID.
 *
 * Calculated values:
 * @property maxSequence Maximum value of sequence
 * @property maxMachineId Maximum value of machine ID
 * @property maxDatacenterId Maximum value of data center ID
 * @property machineShift Number of bits to shift machine ID to left in ID
 * @property datacenterShift Number of bits to shift data center ID to left in ID
 * @property timestampShift Number of bits to shift timestamp to left in ID
 */
class SnowflakeBitsConfig(
    val sequenceBits: Int = 12,
    val machineBits: Int = 7,
    val datacenterBits: Int = 3
) {
    val maxSequence: Long = -1L xor (-1L shl sequenceBits)
    val maxMachineId: Long = -1L xor (-1L shl machineBits)
    val maxDatacenterId: Long = -1L xor (-1L shl datacenterBits)

    val machineShift: Int = sequenceBits
    val datacenterShift: Int = sequenceBits + machineBits
    val timestampShift: Int = datacenterShift + datacenterBits

    companion object {
        const val DEFAULT_MACHINES_PER_CENTER = 128
    }
}