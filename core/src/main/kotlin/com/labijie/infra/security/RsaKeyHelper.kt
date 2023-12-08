package com.labijie.infra.security

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.util.encoders.Base64
import org.bouncycastle.util.io.pem.PemReader
import java.io.InputStreamReader
import java.io.StringReader
import java.nio.charset.Charset
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec


/**
 * @author Anders Xiao
 * @date 2023-12-08
 */
object RsaKeyHelper {
    private fun readPKCS8PrivateKey(byteArray: ByteArray): RSAPrivateKey {
        val keySpec = PKCS8EncodedKeySpec(byteArray)
        val factory = KeyFactory.getInstance("RSA")
        return factory.generatePrivate(keySpec) as RSAPrivateKey
    }

    private fun privateKeyPKCS8ToPKCS1(pkcs18PrivateKeyByte: ByteArray): ByteArray {
        val pki = PrivateKeyInfo.getInstance(pkcs18PrivateKeyByte)
        val parsePrivateKey = pki.parsePrivateKey()
        return parsePrivateKey.toASN1Primitive().getEncoded()
    }

    fun extractRsaPem(rawPemContent: String, charset: Charset = Charsets.UTF_8) : ByteArray {
        val bytes = rawPemContent.toByteArray(charset)
        bytes.inputStream().use {
            inputStream->
            InputStreamReader(inputStream).use { keyReader ->
                PemReader(keyReader).use { pemReader ->
                    val pemObject = pemReader.readPemObject()
                    return pemObject.content
                }
            }
        }
    }

    fun extractRsaPemAsString(rawPemContent: String, charset: Charset = Charsets.UTF_8) : String {
        return Base64.toBase64String(extractRsaPem(rawPemContent, charset))
    }

    fun readPKCS8PrivateKey(pemContent: String, charset: Charset = Charsets.UTF_8): RSAPrivateKey {
        val byteArray = extractRsaPem(pemContent, charset)
        return readPKCS8PrivateKey(byteArray)
    }

    fun readX509PublicKey(pemContent: String): RSAPublicKey {
        val factory = KeyFactory.getInstance("RSA")
        StringReader(pemContent).use { keyReader ->
            PemReader(keyReader).use { pemReader ->
                val pemObject = pemReader.readPemObject()
                val content = pemObject.content
                val pubKeySpec = X509EncodedKeySpec(content)
                return factory.generatePublic(pubKeySpec) as RSAPublicKey
            }
        }
    }



}