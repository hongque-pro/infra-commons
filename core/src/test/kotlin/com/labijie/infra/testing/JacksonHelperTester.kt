package com.labijie.infra.testing

import com.fasterxml.jackson.databind.ObjectMapper
import com.labijie.infra.json.JacksonHelper
import org.junit.jupiter.api.Assertions
import kotlin.test.Test
import java.math.BigDecimal

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-13
 */
class JacksonHelperTester{

    @Test
    fun serializeBigDecimal(){
        val jsonValueString = "0.0000000009"
        val value = BigDecimal(jsonValueString)
        val json = JacksonHelper.serialize(value).toString(Charsets.UTF_8)

        Assertions.assertEquals("\"$jsonValueString\"",json)
    }

    @Test
    fun deserializeBigDecimal(){
        val jsonValueString = "0.0000000009"
        val value = BigDecimal(jsonValueString)
        val dValue = JacksonHelper.deserialize(jsonValueString.toByteArray(Charsets.UTF_8), BigDecimal::class)

        val equals = value.equals(dValue)
        Assertions.assertTrue(equals)
    }

    @Test
    fun serializeBigDecimalProperty(){
        val data = TestData()
        val json = JacksonHelper.serialize(data).toString(Charsets.UTF_8)

        val containsString = json.contains("\"0.0000000009\"")
        Assertions.assertTrue(containsString)
    }

    @Test
    fun listSerialize(){
        val set = arrayListOf(TestData())
        val bytes = JacksonHelper.serialize(set)
        val data = JacksonHelper.deserializeList(bytes, TestData::class)
        Assertions.assertArrayEquals(set.toTypedArray(), data.toTypedArray())
    }

    @Test
    fun setSerialize(){
        val set = arrayListOf(TestData())
        val bytes = JacksonHelper.serialize(set)
        val data = JacksonHelper.deserializeSet(bytes, TestData::class)
        Assertions.assertArrayEquals(set.toTypedArray(), data.toTypedArray())
    }

    @Test
    fun arraySerialize(){
        val set = arrayListOf(TestData())
        val bytes = JacksonHelper.serialize(set)
        val data = JacksonHelper.deserializeArray(bytes, TestData::class)
        Assertions.assertArrayEquals(set.toTypedArray(), data)
    }

    @Test
    fun mapSerialize(){
        val set = mapOf(
            "abc" to TestData(),
            "123" to TestData()
        )
        val bytes = JacksonHelper.serialize(set)
        val data = JacksonHelper.deserializeMap(bytes, String::class, TestData::class)
        Assertions.assertArrayEquals(set.keys.toTypedArray(), data.keys.toTypedArray())
        Assertions.assertArrayEquals(set.values.toTypedArray(), data.values.toTypedArray())
    }

    @Test
    fun longToStringSerialize(){
        val str = JacksonHelper.serializeAsString(TestData(), true)
        val data = JacksonHelper.deserializeFromString(str, TestData::class, true)

        Assertions.assertEquals(Long.MAX_VALUE, data.longValue)
    }

    data class TestData(var decimalValue:BigDecimal = BigDecimal("0.0000000009"),
                        var longValue:Long = Long.MAX_VALUE){

    }
}