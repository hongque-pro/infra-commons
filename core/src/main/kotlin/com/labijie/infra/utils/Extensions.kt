package com.labijie.infra.utils

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.lang.reflect.InvocationTargetException
import java.net.NetworkInterface
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-12
 */

//private const val CGLIB_CLASS_SEPARATOR: String = "$$"

//val Any.logger: Logger
//    get() {
//        val clazz = this::class.java
//        if (clazz.name.contains(CGLIB_CLASS_SEPARATOR)) {
//            val superclass = clazz.superclass
//            if (superclass != null && superclass != Any::class.java) {
//                return LoggerFactory.getLogger(superclass)
//            }
//        }
//        return LoggerFactory.getLogger(this::class.java)
//    }

fun String?.ifNullOrBlank(default: String): String {
    if (this.isNullOrBlank()) {
        return default
    }
    return this
}

inline fun <C : R, R> C?.ifNullOrBlank(defaultValue: () -> R): R
        where R : CharSequence {
    return if (this == null || this.isBlank()) defaultValue() else this
}



private val DEFAULT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

fun nowString(zoneOffset: ZoneOffset = ZoneOffset.UTC): String {
    val format = DEFAULT_FORMAT
    return LocalDateTime.now(zoneOffset).format(format)
}

fun Throwable.printStackToString(): String {
    var stack: String
    ByteArrayOutputStream().use { stream ->
        PrintWriter(stream).use {
            this.printStackTrace(it)
        }
        stack = stream.toByteArray().toString(Charsets.UTF_8)
    }
    return stack
}

fun findIpAddress(netmask: String): String? {
    val netInterfaces = NetworkInterface.getNetworkInterfaces()
    while (netInterfaces.hasMoreElements()) {
        val ni = netInterfaces.nextElement()
        val ia = ni.inetAddresses
        while (ia.hasMoreElements()) {
            val ip = ia.nextElement()
            val r = netmask.replace(".", "\\.").replace("*", "(.*)")
            val regex = Regex(r)
            if (regex.matches(ip.hostAddress)) {
                return ip.hostAddress
            }
        }
    }
    return null
}

fun Throwable.throwIfNecessary() {
    if (this is VirtualMachineError) {
        throw this
    }
}

fun Throwable.recurseCause(type: KClass<out Throwable>? = null): Throwable {
    var t = if (this is InvocationTargetException) this.targetException else this
    if (type != null && t::class.isSubclassOf(type)) {
        return t
    }
    while (t.cause != null) {
        val c = t.cause!!
        if (type != null && c::class.isSubclassOf(type)) {
            return c
        }
        t = c
    }
    return t;
}

fun <T : Any> ObjectMapper.deserializeList(bytes: ByteArray, elementClass: KClass<T>): List<T> {
    val javaType = this.typeFactory.constructCollectionType(List::class.java, elementClass.java)
    return this.readValue(bytes, javaType)
}

fun <T : Any> ObjectMapper.deserializeSet(bytes: ByteArray, elementClass: KClass<T>): Set<T> {
    val javaType = this.typeFactory.constructCollectionType(Set::class.java, elementClass.java)
    return this.readValue(bytes, javaType)
}

fun <T : Any> ObjectMapper.deserializeArray(bytes: ByteArray, elementClass: KClass<T>): Array<T> {
    val javaType = this.typeFactory.constructArrayType(elementClass.java)
    return this.readValue(bytes, javaType)
}

fun <TKey : Any, TValue : Any> ObjectMapper.deserializeMap(bytes: ByteArray, keyClass: KClass<TKey>, valueClass: KClass<TValue>): Map<TKey, TValue> {
    val javaType = this.typeFactory.constructMapType(Map::class.java, keyClass.java, valueClass.java)
    return this.readValue(bytes, javaType)
}

fun Long.toByteArray(): ByteArray {
    val b = ByteArray(8)
    b[7] = (this and 0xff).toByte()
    b[6] = (this shr 8 and 0xff).toByte()
    b[5] = (this shr 16 and 0xff).toByte()
    b[4] = (this shr 24 and 0xff).toByte()
    b[3] = (this shr 32 and 0xff).toByte()
    b[2] = (this shr 40 and 0xff).toByte()
    b[1] = (this shr 48 and 0xff).toByte()
    b[0] = (this shr 56 and 0xff).toByte()
    return b
}

fun ByteArray.toLong(): Long {
    return (this[0].toLong() and 0xff shl 56
            or (this[1].toLong() and 0xff shl 48)
            or (this[2].toLong() and 0xff shl 40)
            or (this[3].toLong() and 0xff shl 32)
            or (this[4].toLong() and 0xff shl 24)
            or (this[5].toLong() and 0xff shl 16)
            or (this[6].toLong() and 0xff shl 8)
            or (this[7].toLong() and 0xff shl 0))
}


fun Int.toByteArray(): ByteArray {
    val b = ByteArray(4)
    b[3] = (this and 0xff).toByte()
    b[2] = (this shr 8 and 0xff).toByte()
    b[1] = (this shr 16 and 0xff).toByte()
    b[0] = (this shr 24 and 0xff).toByte()
    return b
}

fun ByteArray.toInt(): Int {
    return (this[3].toInt() and 0xff
            or (this[2].toInt() and 0xff shl 8)
            or (this[1].toInt() and 0xff shl 16)
            or (this[0].toInt() and 0xff shl 24))
}

fun Instant.toLocalDateTime(zoneId:ZoneId = ZoneOffset.UTC): LocalDateTime {
    return LocalDateTime.ofInstant(this, zoneId)
}

fun LocalDateTime.toEpochMilli(zoneOffset:ZoneOffset = ZoneOffset.UTC): Long {
    return this.toInstant(zoneOffset).toEpochMilli()
}

