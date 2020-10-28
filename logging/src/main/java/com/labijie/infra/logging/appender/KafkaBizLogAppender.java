package com.labijie.infra.logging.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.labijie.infra.logging.BizLogger;
import com.labijie.infra.logging.Tools;
import com.labijie.infra.logging.encoder.KafkaBizLogEncoder;

public class KafkaBizLogAppender extends KafkaAppender<ILoggingEvent> {

    @Override
    public String getTopic() {
        if (topic != null && !topic.isEmpty()) {
            return topic;
        }
        return "bizlogs_" + projectName;
    }

    @Override
    protected byte[] getPayload(ILoggingEvent eventObject) {
        if (eventObject != null && eventObject.getLoggerName().equals(BizLogger.class.getName())) {
            return encoder.encode(eventObject);
        }
        return null;
    }

    @Override
    public void start() {
        KafkaBizLogEncoder bizLayoutEncoder = new KafkaBizLogEncoder();
        bizLayoutEncoder.setProjectName(projectName);
        if (archiveType.trim().isEmpty()) {
            archiveType = "none";
        }
        bizLayoutEncoder.setArchiveType(archiveType);
        bizLayoutEncoder.setIp(Tools.getServerIp());
        this.encoder = bizLayoutEncoder;
        super.start();
    }

}
