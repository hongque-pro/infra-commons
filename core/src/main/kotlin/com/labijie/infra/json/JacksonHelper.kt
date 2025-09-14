package com.labijie.infra.json

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.labijie.infra.utils.deserializeArray
import com.labijie.infra.utils.deserializeList
import com.labijie.infra.utils.deserializeMap
import com.labijie.infra.utils.deserializeSet
import java.math.BigDecimal
import java.util.*
import kotlin.reflect.KClass


/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-13
 */
object JacksonHelper {

    private fun createModule(): SimpleModule {
        val infraModule = SimpleModule("InfraModule")
        infraModule.addDeserializer(BigDecimal::class.java, DecimalAsStringDeserializer)
        infraModule.addSerializer(BigDecimal::class.java, DecimalAsStringSerializer)

        infraModule.addDeserializer(Locale::class.java, LocaleDeserializer)
        @Suppress("UNCHECKED_CAST")
        infraModule.addDeserializer(Long::class.java as Class<in Any>, LongAsStringDeserializer)
        @Suppress("UNCHECKED_CAST")
        infraModule.addDeserializer(Class.forName("java.lang.Long") as Class<in Any>, LongAsStringDeserializer)
        return infraModule
    }

    fun registerModule(module: Module) {
        defaultObjectMapper.registerModule(module)
        webCompatibilityMapper.registerModule(module)
    }


    val defaultObjectMapper = ObjectMapper()
            .configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_ENUMS_USING_INDEX, true)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .apply {
                val module = createModule()
                this.registerModule(module).registerKotlinModule()
            }

    val webCompatibilityMapper: ObjectMapper = ObjectMapper()
            .configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_ENUMS_USING_INDEX, true)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)

            .apply {
                //configOverride(BigDecimal::class.java).format = JsonFormat.Value.forShape(JsonFormat.Shape.STRING)
                val infraModule = createModule()
                infraModule.addSerializer(Long::class.java, ToStringSerializer.instance)
                @Suppress("UNCHECKED_CAST")
                infraModule.addDeserializer(Long::class.java as Class<in Any>, LongAsStringDeserializer)

                //兼容 java
                infraModule.addSerializer(Class.forName("java.lang.Long"), ToStringSerializer.instance)
                @Suppress("UNCHECKED_CAST")
                infraModule.addDeserializer(Class.forName("java.lang.Long") as Class<in Any>, LongAsStringDeserializer)

                this.registerModule(infraModule).registerKotlinModule()
            }

    private fun getObjectMapper(compatibleWeb: Boolean): ObjectMapper {
        return if (compatibleWeb) webCompatibilityMapper else defaultObjectMapper
    }

    fun serialize(data: Any, compatibleWeb: Boolean = false): ByteArray {
        return getObjectMapper(compatibleWeb).writeValueAsBytes(data)
    }


    fun serializeAsString(data: Any, compatibleWeb: Boolean = false): String {
        return getObjectMapper(compatibleWeb).writeValueAsString(data)
    }

    fun serializeAsJsonNode(data: Any, compatibleWeb: Boolean = false): JsonNode {
        return getObjectMapper(compatibleWeb).valueToTree(data)
    }

    fun <T : Any> deserialize(bytes: ByteArray, clazz: KClass<T>): T {
        return defaultObjectMapper.readValue(bytes, clazz.java)
    }

    fun <T> deserialize(bytes: ByteArray, typeReference: TypeReference<out T>): T {
        return defaultObjectMapper.readValue(bytes, typeReference)
    }

    fun <T : Any> deserializeFromJsonNode(jsonNode: JsonNode, clazz: KClass<T>): T {
        return defaultObjectMapper.treeToValue(jsonNode, clazz.java)
    }

    fun <T : Any> deserializeFromString(json: String, clazz: KClass<T>): T {
        return defaultObjectMapper.readValue(json, clazz.java)
    }

    fun <T : Any> deserializeFromString(json: String, typeReference: TypeReference<out T>): T {
        return defaultObjectMapper.readValue(json, typeReference)
    }


    fun <T : Any> deserializeList(bytes: ByteArray, elementClass: KClass<T>): List<T> {
        return defaultObjectMapper.deserializeList(bytes, elementClass)
    }

    fun <T : Any> deserializeSet(bytes: ByteArray, elementClass: KClass<T>): Set<T> {
        return defaultObjectMapper.deserializeSet(bytes, elementClass)
    }

    fun <T : Any> deserializeArray(bytes: ByteArray, elementClass: KClass<T>): Array<T> {
        return defaultObjectMapper.deserializeArray(bytes, elementClass)
    }

    fun <TKey : Any, TValue : Any> deserializeMap(bytes: ByteArray, keyClass: KClass<TKey>, valueClass: KClass<TValue>): Map<TKey, TValue> {
        return defaultObjectMapper.deserializeMap(bytes, keyClass, valueClass)
    }

}