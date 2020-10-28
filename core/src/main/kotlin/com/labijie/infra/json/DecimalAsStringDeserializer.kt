package com.labijie.infra.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.io.IOException
import java.math.BigDecimal

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-13
 */
class DecimalAsStringDeserializer : JsonDeserializer<BigDecimal>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): BigDecimal? {
        val rawValue = p?.text?.trim('"')
        if(rawValue != null){
            try {
                return BigDecimal(rawValue)
            }catch (ex: NumberFormatException){
                throw IOException("Cant read json value '$rawValue' as a BigDecimal.")
            }
        }
        return null
    }
}