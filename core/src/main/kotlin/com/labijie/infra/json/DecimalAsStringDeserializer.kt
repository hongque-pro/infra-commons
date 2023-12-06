package com.labijie.infra.json

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.math.BigDecimal

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-13
 */
object DecimalAsStringDeserializer : JsonDeserializer<BigDecimal>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): BigDecimal? {
        val rawValue = p?.text?.trim('"')
        if(rawValue != null){
            try {
                return BigDecimal(rawValue)
            }catch (ex: NumberFormatException){
                throw JsonParseException(p, "Cant read json value '$rawValue' as a BigDecimal.")
            }
        }
        return null
    }
}