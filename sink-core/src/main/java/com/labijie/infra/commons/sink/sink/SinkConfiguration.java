package com.labijie.infra.commons.sink.sink;

import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * 表示 Sink 用的配置信息。
 */
public final class SinkConfiguration {

    public final static String KAFKA_SERVER = "sink.kak.addresses";
    public final static String KAFKA_SESSION_TIMEOUT = "sink.kak.session.timeout";
    public final static String KAFKA_GROUP_ID = "sink.kak.group.id";
    public final static String KAFKA_POLL_TIMEOUT = "sink.kak.poll.timeout";
    public final static String KAFKA_POLL_SIZE = "sink.kak.poll.size";
    public final static String KAFKA_AUTO_OFFSET_RESET = "sink.kak.auto.offset.reset";
    public final static String KAFKA_OFFSET_AUTO_COMMIT = "sink.kak.offset.auto.commit";
    public final static String KAFKA_VALUE_DESERIALIZER = "sink.kak.default.value.deserializer";
    public final static String KAFKA_KEY_DESERIALIZER = "sink.kak.default.key.deserializer";
    public final static String KAFKA_REBALANCE_TIMEOUT = "sink.kak.rebalance.timeout";

    public final static String KAFKA_TOPIC_PREFIX = "sink.kak.topic.prefix";
    public final static String KAFKA_TOPIC = "sink.kak.topic";

    public final static String CONSUMER_COUNT = "sink.consumer.count";
    public final static String CONCURRENCY_PER_CONSUMER = "sink.consumer.concurrency";
    public final static String CONSUME_LATEST_ALWAYS = "sink.consumer.latest.always";

    private Properties allProperties;

    public SinkConfiguration(Properties properties) throws SinkConfigurationException {
        if (properties == null) {
            throw new SinkConfigurationException("Sink （arguments: Properties）can not be null");
        }
        init(properties, true);
    }

    public SinkConfiguration(Properties properties, boolean validate) throws SinkConfigurationException {
        if (properties == null) {
            throw new SinkConfigurationException("Sink （arguments: Properties）can not be null");
        }
        init(properties, validate);
    }

    public SinkConfiguration(String filePath) throws SinkConfigurationException {
        Properties properties = new Properties();
        if (filePath == null || filePath.isEmpty()) {
            throw new SinkConfigurationException("Sink configuration file can not be null");
        }
        try (FileInputStream stream = new FileInputStream(filePath)) {
            properties.load(stream);
        } catch (FileNotFoundException fe) {
            throw new SinkConfigurationException("Kafka configuration file can not be found. " + filePath, fe);
        } catch (IOException ex) {
            throw new SinkConfigurationException("Error occurred when reading Kafka configuration files. " + filePath, ex);
        }
        init(properties, true);
    }

    private void init(Properties properties, boolean validate) throws SinkConfigurationException {
        allProperties = properties;
        if (validate) {
            validate(allProperties);
        }
    }

    public String getTopicPrefix() {
        return this.getAttachedProperty(KAFKA_TOPIC_PREFIX, "");
    }

    public String getTopic() {
        return this.getAttachedProperty(KAFKA_TOPIC, "");
    }

    public int getConsumerCount() {
        String count = this.getAttachedProperty(CONSUMER_COUNT, "1");
        try {
            return Integer.parseInt(count);
        } catch (NumberFormatException fex) {
            return 1;
        }
    }

    public boolean getConsumeLatestAlways(){
        String latestAlways = this.getAttachedProperty(CONSUME_LATEST_ALWAYS, "false");
        return Boolean.parseBoolean(latestAlways);
    }

    public int getConcurrencyPerConsumer() {
        String count = this.getAttachedProperty(CONCURRENCY_PER_CONSUMER, "1");
        try {
            return Integer.parseInt(count);
        } catch (NumberFormatException fex) {
            return 1;
        }
    }


    public long getPollingTimeoutSeconds() {
        String timeout = this.getAttachedProperty(KAFKA_POLL_TIMEOUT, "30000");
        try {
            int seconds = Integer.parseInt(timeout);
            return seconds / 1000;
        } catch (NumberFormatException fex) {
            return 30;
        }
    }


    /**
     * 获取附加的配置属性。
     *
     * @param propertyName 要获取的属性名称。
     * @param defaultValue 配置中找打不到属性时返回的默认值。
     * @return 给定属性名称的配置值。
     */
    public String getAttachedProperty(String propertyName, String defaultValue) {
        if (!allProperties.containsKey(propertyName)) {
            return defaultValue;
        }
        return this.allProperties.getProperty(propertyName, defaultValue);
    }

    /**
     * 获取  Kafka 的配置实例。
     *
     * @return Kafka 配置实例对象。
     */
    public Properties createKafkaConfiguration() {
        Properties properties = new Properties();

        //参考：http://kafka.apache.org/documentation/#newconsumerconfigs
        properties.setProperty("bootstrap.servers", allProperties.getProperty(KAFKA_SERVER));
        properties.setProperty("session.timeout.ms", allProperties.getProperty(KAFKA_SESSION_TIMEOUT, "10000"));
        properties.setProperty("group.id", allProperties.getProperty(KAFKA_GROUP_ID));
        properties.setProperty("max.poll.records", allProperties.getProperty(KAFKA_POLL_SIZE, "500"));
        properties.setProperty("max.poll.interval.ms", allProperties.getProperty(KAFKA_REBALANCE_TIMEOUT, "300000"));
        properties.setProperty("key.deserializer", allProperties.getProperty(KAFKA_KEY_DESERIALIZER, StringDeserializer.class.getName()));
        properties.setProperty("value.deserializer", allProperties.getProperty(KAFKA_VALUE_DESERIALIZER, ByteArrayDeserializer.class.getName()));
        properties.setProperty("auto.offset.reset", allProperties.getProperty(KAFKA_AUTO_OFFSET_RESET, "earliest"));
        properties.setProperty("enable.auto.commit", allProperties.getProperty(KAFKA_OFFSET_AUTO_COMMIT, "false"));

        return properties;
    }

    private void validate(Properties properties) throws SinkConfigurationException {
        if (!properties.containsKey(KAFKA_SERVER)) {
            throw new SinkConfigurationException("Kafka configuration miss  " + KAFKA_SERVER);
        }
        if (!properties.containsKey(KAFKA_GROUP_ID)) {
            throw new SinkConfigurationException("Kafka configuration miss  " + KAFKA_GROUP_ID);
        }
    }

}
