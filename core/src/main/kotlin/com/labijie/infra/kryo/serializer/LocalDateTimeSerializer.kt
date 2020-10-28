package com.labijie.infra.kryo.serializer

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.KryoException
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.labijie.infra.utils.toEpochMilli
import com.labijie.infra.utils.toLocalDateTime
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-09-10
 */
object LocalDateTimeSerializer :  Serializer<LocalDateTime>() {
    override fun write(kryo: Kryo, output: Output, date: LocalDateTime) {
        output.writeLong(date.toEpochMilli(), true)
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out LocalDateTime>?): LocalDateTime {
        try {
            val mills = input.readLong(true)
            return Instant.ofEpochMilli(mills).toLocalDateTime()
        } catch (e:KryoException){
            throw e
        }catch (e: Exception) {
            throw KryoException(e)
        }
    }
}