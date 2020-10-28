package com.labijie.infra.commons.sink.sink;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 表示 Sink 的持久化器。
 */
public interface ISinkStore<Message> extends ISinkStoreLowLevel<Message> {
    /**
     * 对消息进行持久化。
     *
     * @param message 要持久化的消息。
     */
    void storeMessages(List<Message> message) throws SinkStoreException;

    @Override
    default void store(List<ConsumerRecord<?, Message>> records) throws SinkStoreException {
        List<Message> data = records.stream().map(ConsumerRecord::value).collect(Collectors.toList());
        storeMessages(data);
    }
}
