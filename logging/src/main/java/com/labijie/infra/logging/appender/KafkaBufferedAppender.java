package com.labijie.infra.logging.appender;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;

import javax.annotation.PreDestroy;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Deprecated, 建议使用DiscardableAsyncAppender
 * @param <E>
 */
@Deprecated
public class KafkaBufferedAppender<E> extends KafkaAppender<E> {

    private Queue<E> bufferQueue;

    private int bufferSize = 64 * 1024;

    private String abandonLevel = "INFO";

    public int getBufferSize() {
        return bufferSize;
    }

    public String getAbandonLevel() {
        return abandonLevel;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setAbandonLevel(String abandonLevel) {
        this.abandonLevel = abandonLevel;
    }

    @Override
    public void start() {
        bufferQueue = new LinkedBlockingQueue<>(bufferSize);
        super.start();
        Thread logPusher = new Thread(() -> {
            while (isStarted()) {
                E e = bufferQueue.poll();
                if (e == null) {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    continue;
                }
                super.doAppend(e);
            }
        });
        logPusher.setDaemon(true);
        logPusher.start();
    }

    @Override
    public void stop() {
        while (!bufferQueue.isEmpty()) {
            E e = bufferQueue.poll();
            aai.appendLoopOnAppenders(e);
            super.doAppend(e);
        }
        super.stop();
    }

    @Override
    public void doAppend(E e) {
        boolean success = bufferQueue.offer(e);
        if (success) {
            if (e instanceof ILoggingEvent) {
                Level level = ((ILoggingEvent) e).getLevel();
                if (level.isGreaterOrEqual(Level.toLevel(abandonLevel))) {
                    // if the level is >= abandon level, append the log to attached appender as a backup
                    aai.appendLoopOnAppenders(e);
                }
            }
            return;
        }
        if (e instanceof ILoggingEvent) {
            Level level = ((ILoggingEvent) e).getLevel();
            if (!level.isGreaterOrEqual(Level.toLevel(abandonLevel))) {
                // lower than abandon level, so abandon it
                return;
            }
            while (isStarted()) {
                success = bufferQueue.offer(e);
                if (success) {
                    break;
                }
            }
        }
    }
}
