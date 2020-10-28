package com.labijie.infra.spring.kafka.conf

import java.util.*

/**
 *
 * @author lishiwen
 * @date 18-9-4
 * @since JDK1.8
 */
data class KafkaProducerConfig(
    var servers: String = "",
    var acks: String = "all",
    var retries: Int = 2
) {

  fun toProperties() = Properties()
      .also {
        if (servers.isBlank())
          throw IllegalArgumentException("kafka server is blank")
      }.apply {
        setProperty("bootstrap.servers", servers)
        setProperty("acks", acks)
        setProperty("retries", retries.toString())
      }
}