package com.labijie.infra.sink.elastic.elasticsearch

import com.labijie.infra.sink.elastic.conf.ElasticSearchConfig
import com.labijie.infra.utils.logger
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory
import org.apache.commons.pool2.PooledObject
import org.apache.commons.pool2.impl.DefaultPooledObject
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.transport.client.PreBuiltTransportClient
import org.elasticsearch.common.transport.TransportAddress
import java.net.InetSocketAddress

/**
 *
 * @author lishiwen
 * @date 18-8-14
 * @since JDK1.8
 */
class ElasticSearchFactory : BaseKeyedPooledObjectFactory<ElasticSearchKey, TransportClient> {

  private val elasticConf: ElasticSearchConfig

  constructor(elasticConfig: ElasticSearchConfig) {
    this.elasticConf = elasticConfig
  }

  @Throws(Exception::class)
  override fun create(key: ElasticSearchKey?): TransportClient {
    if (key == null) throw RuntimeException("WARN ElasticSearchKey null")
    val settings = Settings.builder()
        .put("client.transport.sniff", true)
        .put("client.transport.ping_timeout", "${this.elasticConf.clientPingTimeout}s")
        .put("client.transport.nodes_sampler_interval", "${this.elasticConf.clientNodeInterval}s")
        .put("cluster.name", key.clusterName)
        .build()
    if (!this.elasticConf.setNettyProcessors)
      System.setProperty("es.set.netty.runtime.available.processors", "false")
    return try {
      PreBuiltTransportClient(settings)
          .apply {
            key.servers.split(",").forEach { server ->
              val hostAndPort = server.split(":")
              addTransportAddress(TransportAddress(InetSocketAddress(hostAndPort[0], hostAndPort[1].toInt())))
            }
          }
    } catch (e: Exception) {
      logger.error("ERROR: on create ElasticSearch connection", e)
      throw e
    }
  }

  override fun destroyObject(key: ElasticSearchKey?, p: PooledObject<TransportClient>?) {
    try {
      p?.`object`?.close()
    } catch (e: Exception) {
      logger.warn("ERROR on close ElasticSearchKey $key")
    }
  }

  override fun validateObject(key: ElasticSearchKey?, p: PooledObject<TransportClient>?): Boolean {
    return true
  }

  override fun wrap(value: TransportClient?): PooledObject<TransportClient> =
      DefaultPooledObject<TransportClient>(value)
}