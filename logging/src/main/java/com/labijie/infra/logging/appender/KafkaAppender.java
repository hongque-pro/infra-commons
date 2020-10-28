package com.labijie.infra.logging.appender;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import com.labijie.infra.logging.encoder.KafkaLayoutEncoder;
import com.labijie.infra.logging.Tools;
import com.labijie.infra.logging.encoder.KafkaLayoutEncoder;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class KafkaAppender<E> extends UnsynchronizedAppenderBase<E> implements AppenderAttachable<E> {

    class KafkaAvailableMonitor implements Runnable {

        @Override
        public void run() {
            while (isStarted()) {
                lock.lock();
                try {
                    kafkaHasProblemCondition.await();
                    kafkaHasProblem = true;
                    failedTimes.set(0);
                    Thread.sleep(1000L * attacheAppenderSecs);
                    kafkaHasProblem = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        }
    }


    private static final String CONFIG_PREFIX = "LOGGING_";

    final AppenderAttachableImpl<E> aai = new AppenderAttachableImpl<>();

    private final Thread kafkaAvailableMonitor = new Thread(new KafkaAvailableMonitor());

    protected Encoder<E> encoder;

    protected String topic;

    protected String projectName;

    protected String archiveType = "monthly";

    private String producerConfig;

    private boolean kafkaEnabled = true;

    private Properties kafkaProducerProperties;

    private final AtomicInteger failedTimes = new AtomicInteger(0);
    private volatile boolean kafkaHasProblem = false;
    private int maxFailedTimes = 5;
    private int attacheAppenderSecs = 30;
    private Lock lock = new ReentrantLock();
    private Condition kafkaHasProblemCondition = lock.newCondition();

    private KafkaProducer<byte[], byte[]> producer;


    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Encoder<E> getEncoder() {
        return encoder;
    }

    public void setEncoder(Encoder<E> encoder) {
        this.encoder = encoder;
    }

    public String getTopic() {
        if (topic != null && !topic.isEmpty()) {
            return topic;
        }
        return "logs_" + projectName;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getProducerConfig() {
        return producerConfig;
    }

    public void setProducerConfig(String producerConfig) {
        this.producerConfig = producerConfig;
    }

    public void setMaxFailedTimes(int maxFailedTimes) {
        this.maxFailedTimes = maxFailedTimes;
    }

    public void setAttacheAppenderSecs(int attacheAppenderSecs) {
        this.attacheAppenderSecs = attacheAppenderSecs;
    }

    public void setArchiveType(String archiveType) {
        this.archiveType = archiveType;
    }

    private boolean checkConfig() {
        kafkaProducerProperties = new Properties();
        try {
            kafkaProducerProperties.load(new StringReader(producerConfig));
        } catch (IOException e) {
            addError("Load kafka producer configuration failed", e);
            return false;
        }
        Map<String, String> env = System.getenv();
        env.forEach((key, value) -> {
            if (key.startsWith(CONFIG_PREFIX)) {
                String configAttrName = key.substring(CONFIG_PREFIX.length()).replaceAll("_", ".").toLowerCase();
                kafkaProducerProperties.put(configAttrName, value);
            }
        });

        if (!kafkaProducerProperties.containsKey(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)) {
            kafkaEnabled = false;
            addWarn(String.format("KafkaAppender is not enabled. To enable it, you should set %s or set environment variable %sBOOTSTRAP_SERVERS", ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, CONFIG_PREFIX));
            return true;
        }

        if (projectName == null || projectName.isEmpty()) {
            addError("KafkaAppender must set projectName");
            return false;
        }


        if (!kafkaProducerProperties.containsKey(ProducerConfig.ACKS_CONFIG)) {
            kafkaProducerProperties.put(ProducerConfig.ACKS_CONFIG, "0");
        }
        kafkaProducerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        kafkaProducerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        return true;
    }

    @Override
    public void start() {
        if (checkConfig()) {
            if (encoder instanceof KafkaLayoutEncoder) {
                KafkaLayoutEncoder kafkaLayoutEncoder = (KafkaLayoutEncoder) encoder;
                kafkaLayoutEncoder.setProjectName(projectName);
                if (archiveType.trim().isEmpty()) {
                    archiveType = "none";
                }
                kafkaLayoutEncoder.setArchiveType(archiveType.toLowerCase());
                kafkaLayoutEncoder.setIp(Tools.getServerIp());
            }
            if (kafkaEnabled) {
                producer = new KafkaProducer<>(kafkaProducerProperties);
                kafkaAvailableMonitor.setDaemon(true);
                kafkaAvailableMonitor.start();
            }

            super.start();
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (kafkaEnabled) {
            producer.close(10, TimeUnit.SECONDS);
        }
    }

    protected byte[] getPayload(E eventObject) {
        return encoder.encode(eventObject);
    }

    @Override
    protected void append(E eventObject) {
        if (!kafkaEnabled || kafkaHasProblem) {
            aai.appendLoopOnAppenders(eventObject);
            return;
        }
        String topic = getTopic();
        byte[] payload = getPayload(eventObject);
        ProducerRecord<byte[], byte[]> record = new ProducerRecord<>(topic, payload);
        try {
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    aai.appendLoopOnAppenders(eventObject);
                    int failedTimes = this.failedTimes.incrementAndGet();
                    if (failedTimes >= maxFailedTimes) {
                        lock.lock();
                        try {
                            kafkaHasProblemCondition.signalAll();
                        } finally {
                            lock.unlock();
                        }
                    }
                } else {
                    failedTimes.set(0);
                }
            });
        } catch (Exception e) {
            aai.appendLoopOnAppenders(eventObject);
        }
    }


    @Override
    public void addAppender(Appender<E> newAppender) {
        aai.addAppender(newAppender);
    }

    @Override
    public Iterator<Appender<E>> iteratorForAppenders() {
        return aai.iteratorForAppenders();
    }

    @Override
    public Appender<E> getAppender(String name) {
        return aai.getAppender(name);
    }

    @Override
    public boolean isAttached(Appender<E> appender) {
        return aai.isAttached(appender);
    }

    @Override
    public void detachAndStopAllAppenders() {
        aai.detachAndStopAllAppenders();
    }

    @Override
    public boolean detachAppender(Appender<E> appender) {
        return aai.detachAppender(appender);
    }

    @Override
    public boolean detachAppender(String name) {
        return aai.detachAppender(name);
    }
}
