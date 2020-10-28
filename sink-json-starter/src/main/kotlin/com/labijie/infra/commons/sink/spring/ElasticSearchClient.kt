package com.labijie.infra.commons.sink.spring

import org.elasticsearch.client.transport.TransportClient

/**
 *
 * @author lishiwen
 * @date 19-3-19
 * @since JDK1.8
 */
data class ElasticSearchClient(
    val transport: TransportClient
)