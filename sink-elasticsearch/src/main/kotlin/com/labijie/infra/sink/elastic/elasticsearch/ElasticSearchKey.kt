package com.labijie.infra.sink.elastic.elasticsearch

/**
 *
 * @author lishiwen
 * @date 18-8-14
 * @since JDK1.8
 */
data class ElasticSearchKey(
    var servers: String = "",
    var clusterName: String = "") {

  init {
    if (servers.isNullOrBlank() || clusterName.isNullOrBlank())
      throw IllegalArgumentException("invalid servers or clusterName: $servers $clusterName")

    servers = servers.split(",").map {
      val server = it.trim()
      val hostAndPort = server.split(":")
      if (hostAndPort.size != 2 || hostAndPort[1].toIntOrNull() == null)
        throw IllegalArgumentException("invalid servers: $server")
      server
    }.reduce { acc, s -> "$acc,$s" }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true

    if (other == null || javaClass != other.javaClass) return false

    val key = other as ElasticSearchKey? ?: return false

    return servers.compareTo(key.servers, ignoreCase = true) == 0 &&
        clusterName.compareTo(key.clusterName, ignoreCase = true) == 0
  }

  override fun toString(): String =
      StringBuilder("ElasticSearch(")
          .append(" servers=").append(servers)
          .append(" clusterName=").append(clusterName)
          .append(" )")
          .toString()
}