package com.labijie.infra.spring.kafka

import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Autowired

/**
 *
 * @author lishiwen
 * @date 18-9-4
 * @since JDK1.8
 */
class KafkaPublisher<K, V> {

  @Autowired
  private lateinit var kafkaProducer: KafkaProducer<K, V>

  fun send(topic: String, value: V) {
    val msg: ProducerRecord<K, V> = ProducerRecord(topic, value)
    kafkaProducer.send(msg).get()
  }

  fun send(topic: String, key: K, value: V) {
    val msg: ProducerRecord<K, V> = ProducerRecord(topic, key, value)
    kafkaProducer.send(msg).get()
  }

  fun send(topic: String, value: V, callback: Callback) {
    val msg: ProducerRecord<K, V> = ProducerRecord(topic, value)
    kafkaProducer.send(msg, callback)
  }

  fun send(topic: String, key: K, value: V, callback: Callback) {
    val msg: ProducerRecord<K, V> = ProducerRecord(topic, key, value)
    kafkaProducer.send(msg, callback)
  }

}