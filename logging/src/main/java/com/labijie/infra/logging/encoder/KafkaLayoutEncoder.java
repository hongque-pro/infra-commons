package com.labijie.infra.logging.encoder;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.labijie.infra.logging.pojo.LogOuterClass;

public class KafkaLayoutEncoder extends PatternLayoutEncoder {

    protected String projectName;

    protected String ip;

    protected String archiveType;

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setArchiveType(String archiveType) {
        this.archiveType = archiveType;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
        String msg = layout.doLayout(event);
        LogOuterClass.Log.Builder logBuilder = LogOuterClass.Log.newBuilder();
        logBuilder.setLevel(event.getLevel().levelStr);
        logBuilder.setTimestamp(event.getTimeStamp());
        logBuilder.setLogger(event.getLoggerName());
        logBuilder.setThread(event.getThreadName());
        logBuilder.setMessage(msg);
        logBuilder.setProject(projectName);
        logBuilder.setIp(ip == null ? "" : ip);
        logBuilder.setArchiveType(archiveType);
        return logBuilder.build().toByteArray();
    }
}