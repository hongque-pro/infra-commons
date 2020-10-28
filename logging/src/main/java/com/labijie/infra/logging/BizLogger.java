package com.labijie.infra.logging;

import com.labijie.infra.logging.pojo.BizEvent;
import com.labijie.infra.json.JacksonHelper;
import com.labijie.infra.logging.pojo.BizEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class BizLogger {

    private static final Logger logger = LoggerFactory.getLogger(BizLogger.class);

    public static void log(BizEvent event) {
        MDC.put("__eventType", event.getEventType());
        MDC.put("__eventId", event.getEventId());
        logger.info(JacksonHelper.INSTANCE.serializeAsString(event.getData(), false));
        MDC.remove("__eventType");
        MDC.remove("__eventId");
    }

}