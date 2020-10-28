package com.labijie.infra.commons.sink.spring

import com.labijie.infra.sink.elastic.ElasticSearchStore
import com.labijie.infra.sink.elastic.conf.ElasticSearchConfig
import com.labijie.infra.sink.elastic.elasticsearch.ElasticSearchPoolManager
import com.labijie.infra.utils.logger
import com.labijie.infra.utils.throwIfNecessary
import org.elasticsearch.ElasticsearchException
import org.elasticsearch.common.xcontent.XContentType
import java.time.Duration

/**
 *
 * @author lishiwen
 * @date 18-8-16
 * @since JDK1.8
 */
abstract class ElasticJsonStore : ElasticSearchStore<ByteArray> {

  constructor(elasticPool: ElasticSearchPoolManager, elasticConf: ElasticSearchConfig): super(elasticPool, elasticConf)

  override fun storeMessages(message: MutableList<ByteArray>?) {
    if (message == null) return

    val data = mutableListOf<JsonContainer>()
    message.map {
      val container = validateMessage(it)
      if (container == null)
        logger.warn("Invalid message, ignored: $it")
      else
        data.addAll(container)
    }

    if (data.isNotEmpty()) {
      // create ElasticSearch index
      data.map { it.indexName }.toSet().map {
        this.createIndicesIfNotExists(it)
      }

      var success = false
      while (!success) {
        if (!elasticConf.retryOnFailed) {
          success = true
        }
        val client = this.elasticPool.borrowObject(this.key)
        try {
          val bulkRequest = client.prepareBulk()
          data.map {
            bulkRequest.add(client.prepareIndex(it.indexName, this.TYPE, it.id)
                .setSource(it.value, XContentType.JSON))
          }
          val response = bulkRequest.get()
          if (response.hasFailures()) {
            logger.error("Save message to ElasticSearch failed: ${response.buildFailureMessage()}")
            Thread.sleep(Duration.ofSeconds(2).toMillis())
            throw ElasticsearchException("Save message to ElasticSearch failed}")
          }
          success = true
        } catch (t: Throwable) {
          logger.error("ERROR on save to es", t)
          t.throwIfNecessary()
          Thread.sleep(1000)
        } finally {
          this.elasticPool.returnConnection(this.key, client)
        }
      }
    }
  }

  abstract fun validateMessage(message: ByteArray): List<JsonContainer>?
}