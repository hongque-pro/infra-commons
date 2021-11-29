package com.labijie.infra.security

import java.nio.ByteBuffer
import java.time.Duration
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-07-14
 */
class Rfc6238TokenService(private val properties: Rfc6238TokenServiceProperties = Rfc6238TokenServiceProperties()) {

    private val defaultSecurityToken: ByteArray = properties.securityToken.toByteArray()

    private fun computeHash(data: ByteArray, key: ByteArray): ByteArray {
        val signingKey = SecretKeySpec(key, "HmacSHA1")
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(signingKey)
        return mac.doFinal(data)
    }

    private fun computeTotp(key: ByteArray, timestepNumber: Long, modifier: String?): Int {
        // # of 0's = length of pin
        val mod = 1000000

        // See https://tools.ietf.org/html/rfc4226
        // We can add an optional modifier
        val buffer = ByteBuffer.allocate(java.lang.Long.BYTES)
        buffer.putLong(timestepNumber)
        val timestepAsBytes = buffer.array()

        val hash = computeHash(applyModifier(timestepAsBytes, modifier), key)

        // Generate DT string
        val offset = hash[hash.size - 1].toInt() and 0xf
        assert(offset + 4 < hash.size)
        val binaryCode = (hash[offset].toInt() and 0x7f).shl(24) or
                (hash[offset + 1].toInt() and 0xff).shl(16) or
                (hash[offset + 2].toInt() and 0xff).shl(8) or
                (hash[offset + 3].toInt() and 0xff)

        return binaryCode % mod
    }

    private fun applyModifier(input: ByteArray, modifier: String?): ByteArray {
        if (modifier.isNullOrBlank()) {
            return input
        }
        val modifierBytes = modifier.toByteArray(Charsets.UTF_8)
        val combined = ByteBuffer.allocate(input.size + modifierBytes.size)
        combined.put(input)
        combined.put(modifierBytes)
        return combined.array()
    }

    private fun getCurrentTimeStepNumber(timeStep: Duration? = null): Long {
        val step = timeStep ?: properties.timeStep
        return (System.currentTimeMillis() / step.toMillis())
    }


    fun generateCode(securityToken: ByteArray? = null, modifier: String? = null, timeStep: Duration? = null): Int {
        // Allow a variance of no greater than 9 minutes in either direction
        val currentTimeStep = getCurrentTimeStepNumber(timeStep)
        return computeTotp(securityToken ?: defaultSecurityToken, currentTimeStep, modifier)
    }

    fun validateCode(code: Int, securityToken: ByteArray? = null, modifier: String? = null, timeStep: Duration? = null): Boolean {

        // Allow a variance of no greater than 9 minutes in either direction
        val currentTimeStep = getCurrentTimeStepNumber(timeStep)

        (-2..2).forEach {
            val computed = computeTotp(securityToken ?: defaultSecurityToken, currentTimeStep + it, modifier)
            if (computed == code) {
                return true
            }
        }


        // No match
        return false
    }

    fun generateCodeString(securityToken: String? = null, modifier: String? = null, timeStep: Duration? = null): String {
        return generateCode(securityToken?.toByteArray(), modifier, timeStep).toString().padStart(6, '0')
    }

    fun validateCodeString(code: String, securityToken: String? = null, modifier: String? = null, timeStep: Duration? = null): Boolean {
        if(code.length != 6){
            return false
        }
        val num = code.trim().toIntOrNull()
        if(num != null){
            return validateCode(num, securityToken?.toByteArray(), modifier, timeStep)
        }
        return false
    }
}