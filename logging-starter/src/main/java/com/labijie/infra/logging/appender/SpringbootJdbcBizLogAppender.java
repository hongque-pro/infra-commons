package com.labijie.infra.logging.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import com.labijie.infra.logging.Tools;
import com.labijie.infra.logging.encoder.JdbcBizLogEncoder;
import com.labijie.infra.logging.listener.DatasourceListener;
import com.labijie.infra.logging.pojo.DataSourceConfig;
import com.labijie.infra.logging.Tools;
import com.labijie.infra.logging.encoder.JdbcBizLogEncoder;
import com.labijie.infra.logging.listener.DatasourceListener;
import com.labijie.infra.logging.pojo.DataSourceConfig;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SpringbootJdbcBizLogAppender<E> extends UnsynchronizedAppenderBase<E> implements AppenderAttachable<E> {

    private final AppenderAttachableImpl<E> aai = new AppenderAttachableImpl<>();
    private final BlockingQueue<E> bufferQueue = new LinkedBlockingQueue<>();

    protected Map<String, DataSourceConfig> configs = new HashMap<>();

    private String dataSourceConfigs;

    private boolean ready = false;
    private JdbcBizLogEncoder encoder = new JdbcBizLogEncoder();

    public String getDataSourceConfigs() {
        return dataSourceConfigs;
    }

    public void setDataSourceConfigs(String dataSourceConfigs) {
        this.dataSourceConfigs = dataSourceConfigs;
    }

    private boolean checkConfig() {
        if (dataSourceConfigs == null || dataSourceConfigs.isEmpty()) {
            addError("SpringbootJdbcBizLogAppender must specify data source configuration");
            return false;
        }
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(this.dataSourceConfigs));
        } catch (IOException e) {
            addError("Load data source configuration failed", e);
            return false;
        }
        for (final Map.Entry<Object, Object> config : properties.entrySet()) {
            String eventType = config.getKey().toString();
            String[] split = config.getValue().toString().split(":");
            DataSourceConfig dataSourceConfig = new DataSourceConfig();
            dataSourceConfig.setTable(split[0]);
            if (split.length == 2) {
                dataSourceConfig.setDataSourceName(split[1]);
            }
            this.configs.put(eventType, dataSourceConfig);
        }

        if (configs.isEmpty()) {
            addError("SpringbootJdbcBizLogAppender must specify at least one eventType to dataSourceConfig configuration");
            return false;
        }

        return true;
    }

    @Override
    public void start() {
        if (checkConfig()) {
            encoder.setIp(Tools.getServerIp());
            super.start();
        }
    }

    @Override
    protected void append(E eventObject) {
        if (ready) {
            save2DB(eventObject);
            return;
        }
        if (DatasourceListener.dataSourceHolder == null) {
            bufferQueue.offer(eventObject);
            return;
        }
        ready = true;
        E event;
        while ((event = bufferQueue.poll()) != null) {
            save2DB(event);
        }
        save2DB(eventObject);
    }

    protected void save2DB(E eventObject) {
        if (!(eventObject instanceof ILoggingEvent)) {
            return;
        }
        ILoggingEvent event = (ILoggingEvent) eventObject;
        Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();
        Long eventId = Long.valueOf(mdcPropertyMap.get("__eventId"));
        String eventType = mdcPropertyMap.get("__eventType");
        if (!configs.containsKey(eventType)) {
            return;
        }
        DataSourceConfig dsConfig = configs.get(eventType);
        String dataSourceName = dsConfig.getDataSourceName();
        String table = dsConfig.getTable();
        DataSource dataSource = DatasourceListener.getDataSource(dataSourceName);
        if (dataSource == null || table == null || table.isEmpty()) {
            return;
        }
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            statement = connection.prepareStatement(String.format("INSERT INTO %s (id, time_created, content) VALUES (?,?,?)", table));
            statement.setLong(1, eventId);
            statement.setLong(2, event.getTimeStamp());
            statement.setString(3, new String(encoder.encode(event)));
            statement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            aai.appendLoopOnAppenders(eventObject);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
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
