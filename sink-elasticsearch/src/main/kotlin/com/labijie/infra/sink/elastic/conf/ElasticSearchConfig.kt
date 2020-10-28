package com.labijie.infra.sink.elastic.conf

import com.labijie.infra.sink.elastic.elasticsearch.ElasticSearchKey

/**
 *
 * @author lishiwen
 * @date 18-8-14
 * @since JDK1.8
 */
data class ElasticSearchConfig(
    var servers: String = "",
    var clusterName: String = "",
    var shardNumber: Int = 2,
    var searchTimeout: Long = 3000,
    var replicaNumber: Int = 2,
    var retryOnFailed: Boolean = false,
    var createIndexIfNotExist: Boolean = true,
    var clientPingTimeout: Int = 5,
    var clientNodeInterval: Int = 5,
    var maxRetry: Int = 3,
    var poolIdle: Int = 5,
    var poolSize: Int = 5,
    var setNettyProcessors: Boolean = true
) {

  fun getKey(): ElasticSearchKey = ElasticSearchKey(servers, clusterName)
}