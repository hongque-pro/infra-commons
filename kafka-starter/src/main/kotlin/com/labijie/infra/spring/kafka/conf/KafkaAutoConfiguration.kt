package com.labijie.infra.spring.kafka.conf

import com.labijie.infra.spring.kafka.KafkaPublisher
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.util.StringUtils

/**
 *
 * @author lishiwen
 * @date 18-9-4
 * @since JDK1.8
 */
@Configuration
class KafkaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties("infra.kafka.producer")
    fun kafkaConfig(): KafkaProducerConfig = KafkaProducerConfig()

    @Bean("stringProducer")
    fun stringProducer(kafkaConfig: KafkaProducerConfig): KafkaProducer<String, String> {
        val properties = kafkaConfig.toProperties()

        val defaultSerializer = StringSerializer::class.qualifiedName
        if (StringUtils.isEmpty(properties["key.serializer"])) {
            properties.setProperty("key.serializer", defaultSerializer)
        }
        if (StringUtils.isEmpty(properties["value.serializer"])) {
            properties.setProperty("value.serializer", defaultSerializer)
        }

        return KafkaProducer(properties)
    }

  @Primary
  @Bean("byteArrayProducer")
  fun byteArrayProducer(kafkaConfig: KafkaProducerConfig): KafkaProducer<Long, ByteArray> {
    val properties = kafkaConfig.toProperties()

    if (StringUtils.isEmpty(properties["key.serializer"])) {
      properties.setProperty("key.serializer", StringSerializer::class.qualifiedName)
    }
    if (StringUtils.isEmpty(properties["value.serializer"])) {
      properties.setProperty("value.serializer", ByteArraySerializer::class.qualifiedName)
    }

    return KafkaProducer(properties)
  }

  @Bean("kafkaStringPublisher")
  fun kafkaStringPublisher(): KafkaPublisher<String, String> = KafkaPublisher()

  @Bean("kafkaByteArrayPublisher")
  fun kafkaByteArrayPublisher(): KafkaPublisher<Long, ByteArray> = KafkaPublisher()
}