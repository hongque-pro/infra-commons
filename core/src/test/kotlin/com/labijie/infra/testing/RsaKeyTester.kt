package com.labijie.infra.testing

import com.labijie.infra.security.RsaKeyHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File
import java.net.URLDecoder
import kotlin.test.BeforeTest


/**
 * @author Anders Xiao
 * @date 2023-12-08
 */
class RsaKeyTester {

    private lateinit var privatePkcs1: String
    private lateinit var privatePkcs8: String
    private lateinit var pubKey:String

    private fun readFile(fileName: String): String {
        val path = this.javaClass.getClassLoader().getResource(fileName)?.path
        val filePath = URLDecoder.decode(path, "UTF-8")

        println(filePath)
        return File(filePath).readBytes().toString(Charsets.UTF_8)
    }
    @BeforeTest
    fun before() {
        privatePkcs1 = readFile("rsa_private_pkcs1.pem")
        privatePkcs8 = readFile("rsa_private.pem")
        pubKey = readFile("rsa_public.pem")
    }

    @Test
    fun extractRsaPem() {
        val content = RsaKeyHelper.extractRsaPem(privatePkcs8)
        Assertions.assertNotNull(content)

        val contentStr = RsaKeyHelper.extractRsaPemAsString(privatePkcs8)

        val keyValue = privatePkcs8
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\n", "")

        Assertions.assertEquals(contentStr, keyValue)
    }

    @Test
    fun readPkcs8Key() {
       val privateKey = RsaKeyHelper.readPKCS8PrivateKey(privatePkcs8)
        Assertions.assertNotNull(privateKey)
    }

    @Test
    fun readPubKey() {
        val privateKey = RsaKeyHelper.readX509PublicKey(pubKey)
        Assertions.assertNotNull(privateKey)
    }
}