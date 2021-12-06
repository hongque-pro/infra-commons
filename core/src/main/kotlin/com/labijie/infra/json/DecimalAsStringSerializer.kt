package com.labijie.infra.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.math.BigDecimal

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-13
 */
class DecimalAsStringSerializer : JsonSerializer<BigDecimal>() {
    companion object {
        val matchZero = Regex("0+?$")
        var matchDot = Regex("[.]$")
    }

    override fun serialize(value: BigDecimal?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        val s = value?.stripTrailingZeros()?.toPlainString()
        gen?.writeString(s)
    }
}