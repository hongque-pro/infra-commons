package com.labijie.infra.sink.elastic.conf

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
        var replicaNumber: Int = 2,
        var refreshInterval: String = "1s",
        var createIndexIfNotExist: Boolean = true
)