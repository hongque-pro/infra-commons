package com.labijie.infra.commons.sink.sink;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 同步调用store处理所有records
 *
 * @author lishiwen
 * @date 18-12-7
 * @since JDK1.8
 */
public abstract class SinkRecordsBase<T, V> {

  private SinkConfiguration configuration;
  private String[] kafkaTopic;
  private volatile boolean running = false;
  private Semaphore runningSemaphore;
  private int concurrencyLevel;
  private int consumerCount;
  private Timer refreshTimer;
  private String topicPrefix;
  private String topic;
  private static final Logger LOGGER = LoggerFactory.getLogger(SinkRecordsBase.class);
  private static final int TOPIC_REFRESH_INTERVAL_MILLS = 60 * 1000;

  public SinkRecordsBase(Properties properties)
      throws SinkConfigurationException, InvalidParameterException {
    this(new SinkConfiguration(properties));
  }

  public SinkRecordsBase(SinkConfiguration sinkConfig) {
    this.consumerCount = sinkConfig.getConsumerCount();
    this.concurrencyLevel = sinkConfig.getConcurrencyPerConsumer();
    this.topicPrefix = sinkConfig.getTopicPrefix();
    this.topic = sinkConfig.getTopic();
    configuration = sinkConfig;
    runningSemaphore = new Semaphore(consumerCount);
  }

  public SinkRecordsBase(String configFilePath)
      throws SinkConfigurationException, InvalidParameterException {
    this(new SinkConfiguration(configFilePath));
  }

  /**
   * 创建用于 Sink 的持久化存储对象.
   *
   * @return 存储实例。
   */
  protected abstract ISinkStore<V> createStore() throws SinkConfigurationException;

  /**
   * 获取当前 Sink 的配置.
   *
   * @return 配置实例。
   */
  public SinkConfiguration getConfiguration() {
    return configuration;
  }

  /**
   * 获取用于 Kafka 的反序列化提供程序.
   *
   * @param configuration
   * @return
   */
  public String getValueDeserializer(SinkConfiguration configuration) {
    return null;
  }

  public String getKeyDeserializer(SinkConfiguration configuration) {
    return null;
  }

  protected void onKafkaConfigured(Properties properties) {

  }

  public void run() throws SinkConfigurationException {
    if (running) {
      return;
    }

    running = true;
    SinkConfiguration sinkConfiguration = this.getConfiguration();
    // 加载配置
    final Properties config = sinkConfiguration.createKafkaConfiguration();

    String deserializer = this.getValueDeserializer(sinkConfiguration);
    if (deserializer != null && !deserializer.trim().isEmpty()) {
      config.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);
    }

    deserializer = this.getKeyDeserializer(sinkConfiguration);
    if (deserializer != null && !deserializer.trim().isEmpty()) {
      config.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, deserializer);
    }

    this.onKafkaConfigured(config);
    ISinkStore<V> store = this.createStore();

    if (StringUtils.isNotEmpty(topicPrefix)) {
      this.refreshTopics(config);
      if (refreshTimer == null) {
        this.refreshTimer = new Timer();
        this.refreshTimer.schedule(new RefreshTopicTask(config),
            TOPIC_REFRESH_INTERVAL_MILLS,
            TOPIC_REFRESH_INTERVAL_MILLS);
      }
    } else {
      LOGGER.debug(topic);
      this.kafkaTopic = new String[]{topic};
    }

    for (int i = 1; i <= consumerCount; i++) {
      String consumerName = "Sink-RConsumer" + i;
      Thread consumerThread = new Thread(() -> this.consumeKafka(store, config, consumerName));
      consumerThread.setDaemon(true);
      consumerThread.start();
    }
  }

  private void refreshTopics(Properties config) {
    Set<String> topicNames = null;
    while (topicNames == null && running) {
      Properties adminConfig = new Properties();
      adminConfig.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, config.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
      try (AdminClient adminClient = AdminClient.create(adminConfig)) {
        ListTopicsResult listTopicsResult = adminClient.listTopics();
        topicNames = listTopicsResult.names().get(30L, TimeUnit.SECONDS);
      } catch (KafkaException ex) {
        LOGGER.error("Fetching kafka topic info is failed.", ex);
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      } catch (InterruptedException e) {
        LOGGER.error("Fetching kafka topics is interrupted", e);
      } catch (ExecutionException e) {
        LOGGER.error("Fetching kafka topic info is failed", e);
      } catch (TimeoutException e) {
        LOGGER.error("Fetching kafka topic info is timeout", e);
      }
    }

    Set<String> topics = (topicNames == null ? new HashSet<>() : topicNames);
    List<String> keys = topics.stream().filter(t -> t.startsWith(this.topicPrefix)).sorted().collect(Collectors.toList());
    String[] keyArray = new String[keys.size()];
    this.kafkaTopic = keys.toArray(keyArray);
    String topicString = keys.stream().reduce((a, b) -> a + ", " + b).orElse("0 个 topic");
    LOGGER.debug("Fetched topics: {}", topicString);
  }

  private void consumeKafka(ISinkStore<V> store, Properties config, String consumerName) {
    try {
      runningSemaphore.acquire();
    } catch (InterruptedException iex) {
      LOGGER.error("Acquire consumer semaphore is failed", iex);
    }

    String[] currentTopics = this.kafkaTopic;
    KafkaConsumer<T, V> consumer = new KafkaConsumer<>(config);
    consumer.subscribe(Arrays.asList(currentTopics));
    try {
      while (running) {
        if (StringUtils.isNotEmpty(topicPrefix)) {
          if (!Arrays.equals(currentTopics, this.kafkaTopic)) {
            currentTopics = this.kafkaTopic;
            consumer.unsubscribe();
            LOGGER.info("Subscribed kafka topics are changed: {} ", Arrays.toString(currentTopics));
            consumer.subscribe(Arrays.asList(currentTopics));
          }
          if (currentTopics.length == 0) {
            LOGGER.warn("There is not any topic to subscribe, wait 10 seconds and retry.");
            Thread.sleep(10 * 1000);
            continue;
          }
        }
        final ConsumerRecords<T, V> records = consumer.poll(Duration.ofMillis(this.configuration.getPollingTimeoutSeconds() * 1000));

        List<ConsumerRecord<T, V>> partitionRecords = new ArrayList<>();
        records.partitions().forEach(partition -> {
          partitionRecords.addAll(records.records(partition));
        });

        Long start = System.currentTimeMillis();
        try {
          storeRecord(store, partitionRecords);
        } catch (SinkStoreException e) {
          LOGGER.error("handle messages is failed", e);
        }
        try {
          consumer.commitSync();
        } catch (Throwable t) {
          LOGGER.error("ERROR on kafka commit", t);
        }
      }
    } catch (Exception ex) {
      LOGGER.error("Error occurred in consumer thread", ex);
    } finally {
      consumer.close();
      runningSemaphore.release();
    }
  }

  private void storeRecord(ISinkStore<V> store, List<ConsumerRecord<T, V>> records) throws SinkStoreException {
    if (records == null || records.isEmpty()) {
      return;
    }
    List<V> data = records.stream().map(ConsumerRecord::value).collect(Collectors.toList());
    store.storeMessages(data);
  }

  public boolean isRunning() {
    return running;
  }

  /**
   * 获取 message 的调试字符串.
   *
   * @param message
   * @return
   */
  public String getMessageDebugString(V message) {
    return message.toString();
  }

  /**
   * 关闭  Sink 对象.
   */
  public void shutdown() {
    if (!running) {
      return;
    }
    running = false;
    closeTimer();
    try {
      runningSemaphore.acquire(this.consumerCount);
      runningSemaphore.release();
    } catch (InterruptedException ie) {
      LOGGER.error("Shutdown sink is interrupted", ie);
    }
  }

  private void closeTimer() {
    Timer currentTimer = this.refreshTimer;
    this.refreshTimer = null;
    if (currentTimer != null) {
      currentTimer.cancel();
      currentTimer.purge();
    }
  }

  private class RefreshTopicTask extends TimerTask {
    private final Properties config;

    RefreshTopicTask(final Properties config) {
      this.config = config;
    }

    @Override
    public void run() {
      refreshTopics(this.config);
    }
  }
}

