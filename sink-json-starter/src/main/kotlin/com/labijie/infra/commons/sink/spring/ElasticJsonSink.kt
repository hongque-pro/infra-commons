package com.labijie.infra.commons.sink.spring

import com.labijie.infra.sink.elastic.ElasticSearchSink
import com.labijie.infra.sink.elastic.conf.ElasticSearchConfig
import java.util.*

/**
 *
 * @author lishiwen
 * @date 18-8-16
 * @since JDK1.8
 */
abstract class ElasticJsonSink : ElasticSearchSink<String, ByteArray> {

  val elasticConf: ElasticSearchConfig

  constructor(properties: Properties, elasticConf: ElasticSearchConfig): super(properties, elasticConf) {
    this.elasticConf = elasticConf
  }

}