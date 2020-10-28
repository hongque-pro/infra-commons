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
        private val kafkaSettings: _root_ide_package_.com.labijie.infra.commons.sink.conf.KafkaSettings) {

    fun toProperties(): Properties = Properties()
        .apply {
          setProperty(_root_ide_package_.com.labijie.infra.commons.sink.sink.SinkConfiguration.KAFKA_SERVER, kafkaSettings.servers)
          setProperty(_root_ide_package_.com.labijie.infra.commons.sink.sink.SinkConfiguration.KAFKA_SESSION_TIMEOUT, (kafkaSettings.sessionTimeoutSeconds * 1000).toString())
          setProperty(_root_ide_package_.com.labijie.infra.commons.sink.sink.SinkConfiguration.KAFKA_GROUP_ID, kafkaGroupId)
          setProperty(_root_ide_package_.com.labijie.infra.commons.sink.sink.SinkConfiguration.KAFKA_POLL_SIZE, kafkaSettings.pollSize.toString())
          setProperty(_root_ide_package_.com.labijie.infra.commons.sink.sink.SinkConfiguration.KAFKA_POLL_TIMEOUT, (kafkaSettings.pollTimeoutSeconds * 1000).toString())
          setProperty(_root_ide_package_.com.labijie.infra.commons.sink.sink.SinkConfiguration.KAFKA_TOPIC_PREFIX, kafkaTopicPrefix)
          setProperty(_root_ide_package_.com.labijie.infra.commons.sink.sink.SinkConfiguration.KAFKA_TOPIC, kafkaTopic)
          setProperty(_root_ide_package_.com.labijie.infra.commons.sink.sink.SinkConfiguration.KAFKA_AUTO_OFFSET_RESET, kafkaSettings.autoOffsetReset)
          setProperty(_root_ide_package_.com.labijie.infra.commons.sink.sink.SinkConfiguration.KAFKA_REBALANCE_TIMEOUT, (kafkaSettings.rebalanceTimeoutSeconds * 1000).toString())
          setProperty(_root_ide_package_.com.labijie.infra.commons.sink.sink.SinkConfiguration.KAFKA_KEY_DESERIALIZER, kafkaSettings.defaultKeyDeserializer)
          setProperty(_root_ide_package_.com.labijie.infra.commons.sink.sink.SinkConfiguration.KAFKA_VALUE_DESERIALIZER, kafkaSettings.defaultValueDeserializer)
          setProperty(_root_ide_package_.com.labijie.infra.commons.sink.sink.SinkConfiguration.CONSUME_LATEST_ALWAYS, kafkaSettings.consumeLatestAlways)
          setProperty(_root_ide_package_.com.labijie.infra.commons.sink.sink.SinkConfiguration.CONSUMER_COUNT, kafkaSettings.consumerCount.toString())
          setProperty(_root_ide_package_.com.labijie.infra.commons.sink.sink.SinkConfiguration.CONCURRENCY_PER_CONSUMER, kafkaSettings.concurrencyPerConsumer.toString())
          setProperty(_root_ide_package_.com.labijie.infra.commons.sink.sink.SinkConfiguration.KAFKA_OFFSET_AUTO_COMMIT, kafkaSettings.offsetAutoCommit.toString())
        }
}