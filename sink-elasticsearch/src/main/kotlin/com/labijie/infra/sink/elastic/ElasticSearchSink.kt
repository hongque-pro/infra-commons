package com.labijie.infra.sink.elastic

import com.labijie.infra.commons.sink.sink.ISinkStore
import com.labijie.infra.commons.sink.sink.SinkBase
import com.labijie.infra.commons.sink.sink.SinkConfiguration
import com.labijie.infra.sink.elastic.conf.ElasticSearchConfig
import com.labijie.infra.sink.elastic.elasticsearch.ElasticSearchFactory
import com.labijie.infra.sink.elastic.elasticsearch.ElasticSearchKey
import com.labijie.infra.sink.elastic.elasticsearch.ElasticSearchPoolManager
import com.labijie.infra.utils.logger
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig
import org.elasticsearch.client.transport.TransportClient
import java.util.*

/**
 *
 * @author lishiwen
 * @date 18-8-14
 * @since JDK1.8
 */
abstract class ElasticSearchSink<T, V> : _root_ide_package_.com.labijie.infra.commons.sink.sink.SinkBase<T, V> {

  private val elasticConf: ElasticSearchConfig
  private val elasticKey: ElasticSearchKey
  private val elasticPool: ElasticSearchPoolManager

  constructor(properties: Properties, elasticConf: ElasticSearchConfig): super(properties) {
    if (elasticConf.servers.isBlank() || elasticConf.clusterName.isBlank())
      throw IllegalArgumentException("invalid servers or clusterName: $elasticConf.servers $elasticConf.clusterName")

    this.elasticConf = elasticConf
    this.elasticKey = elasticConf.getKey()
    val poolConfig = GenericKeyedObjectPoolConfig<TransportClient>()
      .apply { maxIdlePerKey = elasticConf.poolIdle }
      .apply { maxTotalPerKey = elasticConf.poolSize }
      .apply { testWhileIdle = true }
      .apply { blockWhenExhausted = true }
    this.elasticPool = ElasticSearchPoolManager(ElasticSearchFactory(elasticConf), poolConfig)

    this.checkClient()
  }

  private fun checkClient() {
    val client = this.elasticPool.borrowObject(this.elasticKey)
    try {
      val healths = client.admin().cluster().prepareHealth().get()
      logger.info("ElasticSearch client started... clusterName: ${healths.clusterName}," +
          " number of data nodes: ${healths.numberOfDataNodes}, number of node: ${healths.numberOfNodes}")
    } finally {
      this.elasticPool.returnConnection(this.elasticKey, client)
    }
  }

  override fun createStore(): _root_ide_package_.com.labijie.infra.commons.sink.sink.ISinkStore<V> {
    return createElasticStore(this.elasticPool, this.elasticConf)
  }

  abstract fun createElasticStore(elasticPool: ElasticSearchPoolManager, elasticConf: ElasticSearchConfig): ElasticSearchStore<V>
}