package com.labijie.infra.commons.sink.conf;

import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

/**
 * 表示 Kafka 配置
 * 参考：http://kafka.apache.org/documentation/#newconsumerconfigs
 */
public class KafkaSettings {
    private String servers;
    private int sessionTimeoutSeconds = 10;
    private String groupId;
    private String autoOffsetReset = "earliest";
    private int pollSize;
    private int pollTimeoutSeconds;
    private boolean offsetAutoCommit = false;
    private String defaultKeyDeserializer = StringDeserializer.class.getName();
    private String defaultValueDeserializer = ByteArrayDeserializer.class.getName();
    private String consumeLatestAlways = "false";
    private int rebalanceTimeoutSeconds = 300;
    private int consumerCount = 1;
    private int concurrencyPerConsumer = 1;

    public boolean getOffsetAutoCommit() {
        return offsetAutoCommit;
    }

    public void setOffsetAutoCommit(boolean offsetAutoCommit) {
        this.offsetAutoCommit = offsetAutoCommit;
    }


    /**
     * 获取 Kafka 服务器地址（多个节点以 , 分割，例如：10.66.30.11:9092,10.66.30.11:9093,10.66.30.11:9094）
     *
     * @return 服务器地址
     */
    public String getServers() {
        return servers;
    }

    /**
     * 设置 Kafka 服务器地址（多个节点以 , 分割，例如：10.66.30.11:9092,10.66.30.11:9093,10.66.30.11:9094）
     *
     * @param servers 服务器地址
     */
    public void setServers(String servers) {
        this.servers = servers;
    }

    /**
     * 获取 Kafka Session 超时时间（以秒为单位，默认为10秒）
     *
     * @return
     */
    public int getSessionTimeoutSeconds() {
        return sessionTimeoutSeconds;
    }

    public void setSessionTimeoutSeconds(int sessionTimeoutSeconds) {
        this.sessionTimeoutSeconds = sessionTimeoutSeconds;
    }

    /**
     * 获取 Kafka Consumer 的分组 Id
     *
     * @return 获取到的分组 Id
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * 设置 Kafka Consumer 的分组 Id
     *
     * @param groupId 要设置的分组 Id
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * 获取每次拉去的最大消息条数。
     *
     * @return
     */
    public int getPollSize() {
        return pollSize;
    }

    /**
     * 设置每次拉去的最大消息条数。
     *
     * @param pollSize
     */
    public void setPollSize(int pollSize) {
        this.pollSize = pollSize;
    }

    /**
     * 获取每次拉取不到消息时的超时时间。
     *
     * @return
     */
    public int getPollTimeoutSeconds() {
        return pollTimeoutSeconds;
    }

    /**
     * 设置每次拉取不到消息时的超时时间。
     *
     * @param pollTimeoutSeconds
     */
    public void setPollTimeoutSeconds(int pollTimeoutSeconds) {
        this.pollTimeoutSeconds = pollTimeoutSeconds;
    }

    public String getAutoOffsetReset() {
        return autoOffsetReset;
    }

    public void setAutoOffsetReset(String autoOffsetReset) {
        this.autoOffsetReset = autoOffsetReset;
    }

    public String getDefaultKeyDeserializer() {
        return defaultKeyDeserializer;
    }

    public void setDefaultKeyDeserializer(String defaultKeyDeserializer) {
        this.defaultKeyDeserializer = defaultKeyDeserializer;
    }

    public String getDefaultValueDeserializer() {
        return defaultValueDeserializer;
    }

    public void setDefaultValueDeserializer(String defaultValueDeserializer) {
        this.defaultValueDeserializer = defaultValueDeserializer;
    }

  public String getConsumeLatestAlways() {
    return consumeLatestAlways;
  }

  public void setConsumeLatestAlways(String consumeLatestAlways) {
    this.consumeLatestAlways = consumeLatestAlways;
  }

  public int getConsumerCount() {
    return consumerCount;
  }

  public void setConsumerCount(int consumerCount) {
    this.consumerCount = consumerCount;
  }

  public int getConcurrencyPerConsumer() {
    return concurrencyPerConsumer;
  }

  public void setConcurrencyPerConsumer(int concurrencyPerConsumer) {
    this.concurrencyPerConsumer = concurrencyPerConsumer;
  }

  public int getRebalanceTimeoutSeconds() {
    return rebalanceTimeoutSeconds;
  }

  public void setRebalanceTimeoutSeconds(int rebalanceTimeoutSeconds) {
    this.rebalanceTimeoutSeconds = rebalanceTimeoutSeconds;
  }
}
