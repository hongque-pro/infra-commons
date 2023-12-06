package com.labijie.infra.json

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.labijie.infra.utils.ifNullOrBlank
import org.apache.commons.lang3.LocaleUtils
import java.util.*

/**
 * @author Anders Xiao
 * @date 2023-12-06
 */
object LocaleDeserializer: JsonDeserializer<Locale>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Locale? {
        val rawValue = p?.text?.trim('"')
        if(rawValue?.isNotBlank() == true){
            try {
                return LocaleUtils.toLocale(rawValue)
            }catch (ex: IllegalArgumentException ){
                throw JsonParseException("Unable to convert '${rawValue.ifNullOrBlank("<null>")}' for Locale type")
            }
        }
        return null
    }
}