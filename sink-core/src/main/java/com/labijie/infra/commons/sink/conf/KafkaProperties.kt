package com.labijie.infra.commons.sink.conf

import com.labijie.infra.commons.sink.sink.SinkConfiguration
import java.util.*

/**
 *
 * @author lishiwen
 * @date 18-8-14
 * @since JDK1.8
 */
class KafkaProperties(
        private val kafkaTopic: String = "",
        private val kafkaGroupId: String,
        private val kafkaTopicPrefix: String = "",
        private val kafkaSettings: KafkaSettings) {

    fun toProperties(): Properties = Properties()
        .apply {
          setProperty(SinkConfiguration.KAFKA_SERVER, kafkaSettings.servers)
          setProperty(SinkConfiguration.KAFKA_SESSION_TIMEOUT, (kafkaSettings.sessionTimeoutSeconds * 1000).toString())
          setProperty(SinkConfiguration.KAFKA_GROUP_ID, kafkaGroupId)
          setProperty(SinkConfiguration.KAFKA_POLL_SIZE, kafkaSettings.pollSize.toString())
          setProperty(SinkConfiguration.KAFKA_POLL_TIMEOUT, (kafkaSettings.pollTimeoutSeconds * 1000).toString())
          setProperty(SinkConfiguration.KAFKA_TOPIC_PREFIX, kafkaTopicPrefix)
          setProperty(SinkConfiguration.KAFKA_TOPIC, kafkaTopic)
          setProperty(SinkConfiguration.KAFKA_AUTO_OFFSET_RESET, kafkaSettings.autoOffsetReset)
          setProperty(SinkConfiguration.KAFKA_REBALANCE_TIMEOUT, (kafkaSettings.rebalanceTimeoutSeconds * 1000).toString())
          setProperty(SinkConfiguration.KAFKA_KEY_DESERIALIZER, kafkaSettings.defaultKeyDeserializer)
          setProperty(SinkConfiguration.KAFKA_VALUE_DESERIALIZER, kafkaSettings.defaultValueDeserializer)
          setProperty(SinkConfiguration.CONSUME_LATEST_ALWAYS, kafkaSettings.consumeLatestAlways)
          setProperty(SinkConfiguration.CONSUMER_COUNT, kafkaSettings.consumerCount.toString())
          setProperty(SinkConfiguration.CONCURRENCY_PER_CONSUMER, kafkaSettings.concurrencyPerConsumer.toString())
          setProperty(SinkConfiguration.KAFKA_OFFSET_AUTO_COMMIT, kafkaSettings.offsetAutoCommit.toString())
        }
}