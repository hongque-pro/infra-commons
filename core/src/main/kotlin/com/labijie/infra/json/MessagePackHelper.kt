package com.labijie.infra.json

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import org.msgpack.jackson.dataformat.MessagePackFactory
import java.math.BigDecimal
import kotlin.reflect.KClass

/**
 *
 * @author lishiwen
 * @date 18-10-23
 * @since JDK1.8
 */
object MessagePackHelper {

  private val defaultObjectMapper = ObjectMapper(MessagePackFactory())
      .configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(SerializationFeature.WRITE_ENUMS_USING_INDEX, true)
      .setSerializationInclusion(JsonInclude.Include.NON_NULL)
      .apply {
        val decimalModule = SimpleModule("Numeric")
        decimalModule.addSerializer(BigDecimal::class.java, DecimalAsStringSerializer())

          decimalModule.addSerializer(Long::class.java, ToStringSerializer.instance)
          @Suppress("UNCHECKED_CAST")
          decimalModule.addDeserializer(Long::class.java as Class<in Any>, LongAsStringDeserializer())

          //兼容 java
          decimalModule.addSerializer(Class.forName("java.lang.Long"), ToStringSerializer.instance)
          @Suppress("UNCHECKED_CAST")
          decimalModule.addDeserializer(Class.forName("java.lang.Long") as Class<in Any>, LongAsStringDeserializer())

        this.registerModule(decimalModule)
      }

  fun serialize(pojo: Any): ByteArray {
    return defaultObjectMapper.writeValueAsBytes(pojo)
  }

  fun <T : Any> deserialize(bytes: ByteArray, clazz: KClass<T>): T {
    return defaultObjectMapper.readValue(bytes, clazz.java)
  }
}