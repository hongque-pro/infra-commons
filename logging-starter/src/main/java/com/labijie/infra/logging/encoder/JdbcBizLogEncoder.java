package com.labijie.infra.logging.encoder;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.EncoderBase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

public class JdbcBizLogEncoder extends EncoderBase<ILoggingEvent> {

    protected String ip;

    private ObjectMapper mapper = new ObjectMapper();

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public byte[] headerBytes() {
        return null;
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
        try {
            Map<String, Object> payload = mapper.readValue(event.getFormattedMessage(), new TypeReference<Map<String, Object>>() {
            });
            if (ip != null) {
                payload.put("_ip", ip);
            }
            return mapper.writeValueAsBytes(payload);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return event.getFormattedMessage().getBytes();
    }

    @Override
    public byte[] footerBytes() {
        return null;
    }
}
