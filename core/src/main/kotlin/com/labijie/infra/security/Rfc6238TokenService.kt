package com.labijie.infra.security

import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.time.Duration
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-07-14
 */
class Rfc6238TokenService(private val properties: Rfc6238TokenServiceProperties = Rfc6238TokenServiceProperties()) :
    IRfc6238TokenService {

    companion object{

        fun randomKey(): String {
            val secureKey64Bytes = ByteArray(64)
            SecureRandom().nextBytes(secureKey64Bytes)
            return Base64.getEncoder().encodeToString(secureKey64Bytes)
        }

        val keyLength = mapOf<Rfc6238Algorithm, Int>(
            Rfc6238Algorithm.HmacSHA1 to 20,
            Rfc6238Algorithm.HmacSHA256 to 32,
            Rfc6238Algorithm.HmacSHA512 to 64,
        )

        private val logger by lazy {
            LoggerFactory.getLogger(Rfc6238TokenService::class.java)
        }
    }

    private val securityKey: ByteArray = Base64.getDecoder().decode(properties.keyBase64)

    private fun computeHash(data: ByteArray, key: ByteArray): ByteArray {
        val minLength = (keyLength[properties.algorithm] ?: 0)
        if(key.size < minLength){
            logger.warn("RFC6238: Key (${key.size} bytes) too short for ${properties.algorithm}; Recommended at least $minLength bytes.")
        }
        val signingKey = SecretKeySpec(key, properties.algorithm.toString())
        val mac = Mac.getInstance(properties.algorithm.toString())
        mac.init(signingKey)
        return mac.doFinal(data)
    }

    private fun computeTotp(timestepNumber: Long, modifier: String?): Int {
        // # of 0's = length of pin
        val mod = 1000000

        // See https://tools.ietf.org/html/rfc4226
        // We can add an optional modifier
        val buffer = ByteBuffer.allocate(java.lang.Long.BYTES)
        buffer.putLong(timestepNumber)
        val timestepAsBytes = buffer.array()

        val hash = computeHash(applyModifier(timestepAsBytes, modifier), securityKey)

        // Generate DT string
        val offset = hash[hash.size - 1].toInt() and 0xf
        require(offset + 4 < hash.size) { "Invalid offset, hash too short." }
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


    override fun generateCode(modifier: String?, timeStep: Duration?): Int {
        // Allow a variance of no greater than 9 minutes in either direction
        val currentTimeStep = getCurrentTimeStepNumber(timeStep)
        return computeTotp(currentTimeStep, modifier)
    }

    override fun validateCode(code: Int, modifier: String?, timeStep: Duration?): Boolean {

        // Allow a variance of no greater than 9 minutes in either direction
        val currentTimeStep = getCurrentTimeStepNumber(timeStep)

        (-2..2).forEach {
            val computed = computeTotp(currentTimeStep + it, modifier)
            if (computed == code) {
                return true
            }
        }


        // No match
        return false
    }

    override fun generateCodeString(modifier: String?, timeStep: Duration?): String {
        return generateCode(modifier, timeStep).toString().padStart(6, '0')
    }

    override fun validateCodeString(code: String, modifier: String?, timeStep: Duration?): Boolean {
        if (code.length != 6) {
            return false
        }
        val num = code.trim().toIntOrNull()
        if (num != null) {
            return validateCode(num, modifier, timeStep)
        }
        return false
    }
}