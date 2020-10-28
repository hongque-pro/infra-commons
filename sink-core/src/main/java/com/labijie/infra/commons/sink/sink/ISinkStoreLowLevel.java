package com.labijie.infra.commons.sink.sink;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Anders Xiao
 * @date 2019-10-17
 */
public interface ISinkStoreLowLevel<TValue> {
    void store(List<ConsumerRecord<?, TValue>> records) throws SinkStoreException;
}
