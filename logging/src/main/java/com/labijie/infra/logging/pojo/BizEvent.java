package com.labijie.infra.logging.pojo;

import java.util.Map;

public class BizEvent {

    private String eventType;

    private String eventId = "";

    private Map<String, ?> data;

    public BizEvent(String eventType, String eventId, Map<String, ?> data) {
        this.eventType = eventType;
        this.eventId = eventId;
        this.data = data;
    }

    public BizEvent(String eventType, Map<String, ?> data) {
        this.eventType = eventType;
        this.data = data;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Map<String, ?> getData() {
        return data;
    }

    public void setData(Map<String, ?> data) {
        this.data = data;
    }
}
