package com.labijie.infra.kryo

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.serializers.DefaultSerializers
import com.labijie.infra.kryo.serializer.DateSerializer
import com.labijie.infra.kryo.serializer.LocalDateTimeSerializer
import com.labijie.infra.kryo.serializer.URISerializer
import com.labijie.infra.kryo.serializer.UUIDSerializer
import java.lang.IllegalArgumentException
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URI
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashMap
import kotlin.collections.LinkedHashSet
import kotlin.reflect.KClass

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-09-10
 */
object DefaultKryoFactory {
    @Throws(IllegalArgumentException::class)
    fun createKryo(classRegistry: Map<Int, KClass<*>>): Kryo {
        return Kryo().apply {
            this.isRegistrationRequired = false
            this.warnUnregisteredClasses = false
            /* kryo default
            register(int.class, new IntSerializer());
            register(String.class, new StringSerializer());
            register(float.class, new FloatSerializer());
            register(boolean.class, new BooleanSerializer());
            register(byte.class, new ByteSerializer());
            register(char.class, new CharSerializer());
            register(short.class, new ShortSerializer());
            register(long.class, new LongSerializer());
            register(double.class, new DoubleSerializer());

             */
            this.register(BigDecimal::class.java, DefaultSerializers.BigDecimalSerializer(), 9)
            this.register(BigInteger::class.java, DefaultSerializers.BigIntegerSerializer(), 10)
            this.register(BitSet::class.java, 11)
            this.register(URI::class.java, URISerializer, 12)
            this.register(UUID::class.java, UUIDSerializer, 13)
            this.register(HashMap::class.java, 14)
            this.register(ArrayList::class.java, 15)
            this.register(LinkedList::class.java, 16)
            this.register(HashSet::class.java, 17)
            this.register(TreeSet::class.java, 18)
            this.register(Hashtable::class.java, 19)
            this.register(Date::class.java, DateSerializer, 20)
            this.register(Calendar::class.java, 21)
            this.register(ConcurrentHashMap::class.java, 22)
            this.register(Vector::class.java, 23)
            this.register(StringBuffer::class.java, 24)
            this.register(ByteArray::class.java, 25)
            this.register(CharArray::class.java, 26)
            this.register(IntArray::class.java, 27)
            this.register(FloatArray::class.java, 28)
            this.register(DoubleArray::class.java, 29)
            this.register(DoubleArray::class.java, 30)
            this.register(ShortArray::class.java, 31)
            this.register(LocalDateTime::class.java,  LocalDateTimeSerializer, 32)

            this.register(LinkedHashMap::class.java, 40)
            this.register(LinkedHashSet::class.java, 41)

            classRegistry.forEach {
                require(it.key > 100) { "Kryo register class id must be greater than 100 ( start with 101 )" }
                this.register(it.value.java, it.key)
            }
        }
    }
}