package com.labijie.infra.sink.elastic

import com.fasterxml.jackson.databind.ObjectMapper
import com.labijie.infra.commons.sink.sink.ISinkStore
import com.labijie.infra.sink.elastic.conf.ElasticSearchConfig
import com.labijie.infra.utils.logger
import org.apache.http.util.EntityUtils
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.get.GetIndexRequest
import org.elasticsearch.client.Request
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.xcontent.XContentType


/**
 *
 * @author lishiwen
 * @date 18-8-14
 * @since JDK1.8
 */
abstract class ElasticSearchStore<V>(
        protected val client: RestHighLevelClient,
        protected val elasticConf: ElasticSearchConfig
) : ISinkStore<V> {

    protected val addType: Boolean

    protected val esVersion: String

    init {
        val lowLevelClient = client.lowLevelClient
        val response = lowLevelClient.performRequest(Request("GET", "/"))
        val jsonNode = ObjectMapper().readTree(EntityUtils.toByteArray(response.entity))
        val versionNode = jsonNode["version"]
        esVersion = versionNode["number"].asText()
        logger.info("The version of ES cluster for ${this.javaClass.name} is $esVersion")
        addType = !(esVersion.startsWith("7.") || esVersion.startsWith("8."))
    }

    @Suppress("DEPRECATION")
    @Throws(Exception::class)
    fun createIndicesIfNotExists(index: String) {
        val indexName = index.toLowerCase()
        if (!this.indexCreatedTable.contains(indexName)) {
            val indicesClient = this.client.indices()
            val exists = if(addType){
                //7.x 以前
                indicesClient.exists(GetIndexRequest().indices(indexName), RequestOptions.DEFAULT)
            }else{
                indicesClient.exists(org.elasticsearch.client.indices.GetIndexRequest(indexName), RequestOptions.DEFAULT)
            }
            if (exists) {
                this.indexCreatedTable.add(indexName)
                return
            }
            val mapping = loadMapping(index)
            val settings = Settings.builder()
                    .put("index.number_of_shards", this.elasticConf.shardNumber)
                    .put("index.number_of_replicas", this.elasticConf.replicaNumber)
                    .put("index.refresh_interval", this.elasticConf.refreshInterval)
                    .build()
            if (addType) {
                // 7.x 以前的版本
                val createIndexRequest = CreateIndexRequest(indexName, settings)
                if (mapping != null && mapping.isNotBlank()) {
                    createIndexRequest.mapping(this.TYPE, mapping, XContentType.JSON)
                }
                val response = indicesClient.create(createIndexRequest, RequestOptions.DEFAULT)
                if (response.isAcknowledged) {
                    this.indexCreatedTable.add(indexName)
                    logger.info("create index $indexName done")
                } else {
                    logger.info("create index $indexName failed")
                }
            } else {
                // 7.x 以后的版本
                val createIndexRequest = org.elasticsearch.client.indices.CreateIndexRequest(indexName)
                if (mapping != null) {
                    createIndexRequest.mapping(mapping, XContentType.JSON)
                }
                val response = indicesClient.create(createIndexRequest, RequestOptions.DEFAULT)
                if (response.isAcknowledged) {
                    this.indexCreatedTable.add(indexName)
                    logger.info("create index $indexName done")
                } else {
                    logger.info("create index $indexName failed")
                }
            }
        }
    }

    abstract fun loadMapping(index: String): String?

    abstract val TYPE: String

    private val indexCreatedTable: MutableSet<String> = mutableSetOf()
}