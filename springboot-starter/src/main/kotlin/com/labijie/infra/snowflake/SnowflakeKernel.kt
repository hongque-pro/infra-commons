package com.labijie.infra.snowflake

import kotlin.math.abs

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-10
 */
class SnowflakeKernel(nodeId: Long,
                      /**
                       * 起始的时间戳
                       */
                      private val startTimestamp: Long = 1480166465631L) {
    private val machineId: Long
    private val dataCenterId: Long = 0

    init {
        if (nodeId < 1 || nodeId > 1024) {
            throw IllegalArgumentException("Snow flake node id must be between 1 and 1024.")
        }
        this.machineId = nodeId - 1L;
    }

    private var sequence = 0L //序列号
    private var lastStmp = -1L//上一次时间戳

    private val nextMill: Long
        get() {
            var mill = newstmp
            while (mill <= lastStmp) {
                mill = newstmp
            }
            return mill
        }

    private val newstmp: Long
        get() = System.currentTimeMillis()

    init {
        if (dataCenterId > MAX_DATACENTER_NUM || dataCenterId < 0) {
            Long.MAX_VALUE
            throw IllegalArgumentException("datacenterId can't be greater than $MAX_DATACENTER_NUM or less than 0")
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw IllegalArgumentException("machineId can't be greater than $MAX_MACHINE_NUM or less than 0")
        }
    }

    /**
     * 产生下一个ID
     *
     * @return
     */
    @Synchronized
    fun nextId(): Long {
        var currStmp = newstmp
        if (currStmp < lastStmp) {
            val dist = abs(currStmp - lastStmp)
            if (dist > 5000) {
                throw RuntimeException("Clock moved backwards over 5 seconds.  Refusing to generate id")
            }else{
                Thread.sleep(dist + 1)
                return nextId()
            }
        }

        if (currStmp == lastStmp) {
            //相同毫秒内，序列号自增
            sequence = sequence + 1 and MAX_SEQUENCE
            //同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                currStmp = nextMill
            }
        } else {
            //不同毫秒内，序列号置为0
            sequence = 0L
        }

        lastStmp = currStmp

        return (currStmp - startTimestamp shl TIMESTMP_LEFT //时间戳部分
                or (dataCenterId shl DATACENTER_LEFT      //数据中心部分
                )
                or (machineId shl MACHINE_LEFT            //机器标识部分
                )
                or sequence)                            //序列号部分
    }

    companion object {


        /**
         * 每一部分占用的位数
         */
        private const val SEQUENCE_BIT: Int = 12 //序列号占用的位数
        private const val MACHINE_BIT: Int = 10  //机器标识占用的位数
        private const val DATACENTER_BIT: Int = 0//数据中心占用的位数

        /**
         * 每一部分的最大值
         */
        private const val MAX_DATACENTER_NUM = -1L xor (-1L shl DATACENTER_BIT)
        private const val MAX_MACHINE_NUM = -1L xor (-1L shl MACHINE_BIT)
        private const val MAX_SEQUENCE = -1L xor (-1L shl SEQUENCE_BIT)

        /**
         * 每一部分向左的位移
         */
        private const val MACHINE_LEFT = SEQUENCE_BIT
        private const val DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT
        private const val TIMESTMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT
    }

}