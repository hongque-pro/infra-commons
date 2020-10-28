package com.labijie.infra.sink.elastic.elasticsearch

import com.labijie.infra.utils.logger
import com.labijie.infra.utils.throwIfNecessary
import org.apache.commons.pool2.impl.GenericKeyedObjectPool
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig
import org.elasticsearch.client.transport.TransportClient

/**
 *
 * @author lishiwen
 * @date 18-8-14
 * @since JDK1.8
 */
class ElasticSearchPoolManager : GenericKeyedObjectPool<ElasticSearchKey, TransportClient> {


  constructor(factory: ElasticSearchFactory,
              config: GenericKeyedObjectPoolConfig<TransportClient>): super(factory, config)

  fun destroyConnection(key: ElasticSearchKey, obj: TransportClient) {
    try {
      super.invalidateObject(key, obj)
    } catch (e: Exception) {
      logger.error("ERROR on destroy ElasticSearch connection", e)
      e.throwIfNecessary()
    }
  }

  fun returnConnection(key: ElasticSearchKey, obj: TransportClient) {
    try {
      super.returnObject(key, obj)
    } catch (e: Exception) {
      logger.error("ERROR on return ElasticSearch connection", e)
      e.throwIfNecessary()
    }
  }

}