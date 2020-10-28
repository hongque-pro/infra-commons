package com.labijie.infra.logging.appender;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class DiscardableAsyncAppender extends AsyncAppender {

    private String discardableLevel = null;

    public String getDiscardableLevel() {
        return discardableLevel;
    }

    public void setDiscardableLevel(String discardableLevel) {
        this.discardableLevel = discardableLevel;
    }

    @Override
    protected boolean isDiscardable(ILoggingEvent event) {
        if (discardableLevel == null || discardableLevel.trim().isEmpty()) {
            return false;
        }
        Level level = event.getLevel();
        Level discardable = Level.toLevel(getDiscardableLevel());
        return discardable.isGreaterOrEqual(level);
    }
}
