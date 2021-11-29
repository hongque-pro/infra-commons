package com.labijie.infra.commons.sink.spring.conf

import com.labijie.infra.commons.sink.conf.KafkaSettings
import com.labijie.infra.commons.sink.sink.SinkBase
import com.labijie.infra.commons.sink.spring.ElasticSearchClient
import com.labijie.infra.sink.elastic.conf.ElasticSearchConfig
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.TransportAddress
import org.elasticsearch.transport.client.PreBuiltTransportClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.net.InetSocketAddress

/**
 *
 * @author lishiwen
 * @date 18-8-16
 * @since JDK1.8
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(SinkBase::class)
class ElasticJsonAutoConfiguration {

  @Bean
  @Primary
  @ConfigurationProperties("infra.kafka")
  fun kafkaSettings(): KafkaSettings = KafkaSettings()

  @Bean
  @Primary
  @ConfigurationProperties("infra.elasticsearch")
  fun elasticConfig(): ElasticSearchConfig = ElasticSearchConfig()

  @Bean
  @ConditionalOnMissingBean
  fun esClient(elasticConfig: ElasticSearchConfig): ElasticSearchClient {
    val key = elasticConfig.getKey()
    val settings = Settings.builder()
        .put("client.transport.sniff", true)
        .put("client.transport.ping_timeout", "${elasticConfig.clientPingTimeout}s")
        .put("client.transport.nodes_sampler_interval", "${elasticConfig.clientNodeInterval}s")
        .put("cluster.name", key.clusterName)
        .build()
    if (!elasticConfig.setNettyProcessors)
      System.setProperty("es.set.netty.runtime.available.processors", "false")
    val transportClient = PreBuiltTransportClient(settings)
        .apply {
          key.servers.trim().split(",").forEach { server ->
            val hostAndPort = server.trim().split(":")
            addTransportAddress(TransportAddress(InetSocketAddress(hostAndPort[0].trim(), hostAndPort[1].trim().toInt())))
          }
        }
    return ElasticSearchClient(
        transport = transportClient
    )
  }
}