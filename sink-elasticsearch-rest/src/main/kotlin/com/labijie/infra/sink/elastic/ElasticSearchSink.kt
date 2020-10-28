package com.labijie.infra.sink.elastic

import com.labijie.infra.commons.sink.sink.ISinkStore
import com.labijie.infra.commons.sink.sink.SinkBase
import com.labijie.infra.sink.elastic.conf.ElasticSearchConfig
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import java.net.URL
import java.util.*


abstract class ElasticSearchSink<T, V>(properties: Properties, private val elasticConf: ElasticSearchConfig) : SinkBase<T, V>(properties) {
    private val client: RestHighLevelClient

    init {
        if (elasticConf.servers.isBlank() || elasticConf.clusterName.isBlank())
            throw IllegalArgumentException("invalid servers or clusterName: $elasticConf.servers $elasticConf.clusterName")
        val hosts = elasticConf.servers.split(',').map {
            val url = URL(it.trim())
            HttpHost(url.host, url.port, url.protocol)
        }
        client = RestHighLevelClient(RestClient.builder(*hosts.toTypedArray()))
    }


    override fun createStore(): ISinkStore<V> {
        return createElasticStore(this.client, this.elasticConf)
    }

    override fun shutdown() {
        super.shutdown()
        this.client.close()
    }

    abstract fun createElasticStore(client: RestHighLevelClient, elasticConf: ElasticSearchConfig): ElasticSearchStore<V>
}