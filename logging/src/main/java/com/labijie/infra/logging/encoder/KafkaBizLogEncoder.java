package com.labijie.infra.logging.encoder;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.EncoderBase;
import com.labijie.infra.logging.pojo.LogOuterClass;

import java.util.Map;

public class KafkaBizLogEncoder extends EncoderBase<ILoggingEvent> {

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
    public byte[] headerBytes() {
        return null;
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
//        String msg = layout.doLayout(event);
        LogOuterClass.BizLog.Builder logBuilder = LogOuterClass.BizLog.newBuilder();
        logBuilder.setTimestamp(event.getTimeStamp());
        logBuilder.setProject(projectName);
        logBuilder.setIp(ip);
        logBuilder.setArchiveType(archiveType);
        logBuilder.setData(event.getFormattedMessage());
        Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();
        logBuilder.setEventType(mdcPropertyMap.get("__eventType"));
        logBuilder.setId(mdcPropertyMap.get("__eventId"));
        return logBuilder.build().toByteArray();
    }

    @Override
    public byte[] footerBytes() {
        return null;
    }
}
