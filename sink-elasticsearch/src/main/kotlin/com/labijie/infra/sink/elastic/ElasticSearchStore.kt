package com.labijie.infra.sink.elastic

import com.labijie.infra.commons.sink.sink.ISinkStore
import com.labijie.infra.sink.elastic.conf.ElasticSearchConfig
import com.labijie.infra.sink.elastic.elasticsearch.ElasticSearchKey
import com.labijie.infra.sink.elastic.elasticsearch.ElasticSearchPoolManager
import com.labijie.infra.utils.logger
import org.elasticsearch.ResourceAlreadyExistsException
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.TransportAddress
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.transport.client.PreBuiltTransportClient
import java.net.InetAddress


/**
 *
 * @author lishiwen
 * @date 18-8-14
 * @since JDK1.8
 */
abstract class ElasticSearchStore<V> : _root_ide_package_.com.labijie.infra.commons.sink.sink.ISinkStore<V> {

  val elasticPool: ElasticSearchPoolManager
  val elasticConf: ElasticSearchConfig
  val key: ElasticSearchKey

  constructor(elasticPool: ElasticSearchPoolManager, elasticConf: ElasticSearchConfig) {
    this.elasticPool = elasticPool
    this.elasticConf = elasticConf
    this.key = elasticConf.getKey()
  }

  @Throws(Exception::class)
  fun createIndicesIfNotExists(index: String) {
    val indexName = index.toLowerCase()
    if (!this.indexCreatedTable.contains(indexName)) {
      val transport = this.elasticPool.borrowObject(this.key)
      val client = transport.admin().indices()
      try {
        val existsResponse = client.prepareExists(indexName).get()
        if (existsResponse.isExists){
          this.indexCreatedTable.add(indexName)
          return
        }
        val mapping = loadMappingJson()
        client.prepareCreate(indexName).addMapping(this.TYPE, mapping, XContentType.JSON)
            .setSettings(Settings.builder()
                .put("index.number_of_shards", this.elasticConf.shardNumber)
                .put("index.number_of_replicas", this.elasticConf.replicaNumber)
            ).get()
        this.indexCreatedTable.add(indexName)
        logger.info("create $indexName done")
      } catch (rex: ResourceAlreadyExistsException) {
        this.indexCreatedTable.add(indexName)
        logger.debug("$indexName already exists")
      } finally {
        this.elasticPool.returnObject(this.key, transport)
      }
    }
  }

  private fun loadMappingJson(): String {
    if (this.mappingJson == null) {
      this.mappingJson = this.loadMapping()
    }
    return this.mappingJson!!
  }

  abstract fun loadMapping(): String

  abstract val TYPE: String

  private var mappingJson: String? = null
  private val indexCreatedTable: MutableSet<String> = mutableSetOf()
}