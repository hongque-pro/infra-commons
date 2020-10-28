package com.labijie.infra.logging.appender;

import org.springframework.util.StringUtils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SpringbootKafkaAppender<E> extends KafkaAppender<E> {

    private BlockingQueue<E> bufferQueue = new LinkedBlockingQueue<>();

    private boolean ready = false;

    @Override
    protected void append(E eventObject) {
        if (ready) {
            super.append(eventObject);
            return;
        }
        String appName = System.getProperty("spring.application.name");
        if (StringUtils.isEmpty(appName)) {
            bufferQueue.offer(eventObject);
            return;
        }
        if (!appName.equals(getProjectName())) {
            addError("The configured projectName must be the same as spring.application.name");
            System.exit(-1);
        }
        ready = true;
        E event;
        while ((event = bufferQueue.poll()) != null) {
            super.append(event);
        }
        super.append(eventObject);
    }
}
