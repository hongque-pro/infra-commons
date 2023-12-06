package com.labijie.infra.json

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.io.IOException
import com.fasterxml.jackson.databind.ser.std.StdSerializer



/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-09-17
 */
object LongAsStringDeserializer : JsonDeserializer<Any>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Any? {
        val rawValue = p?.text?.trim('"')
        if(rawValue != null){
            try {
                return rawValue.toLong()
            }catch (ex: NumberFormatException){
                throw JsonParseException(p, "Cant read json value '$rawValue' as a Long.")
            }
        }
        return null
    }
}
