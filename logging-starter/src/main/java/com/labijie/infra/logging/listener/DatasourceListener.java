package com.labijie.infra.logging.listener;

import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;

import javax.sql.DataSource;
import java.util.Map;

public class DatasourceListener implements ApplicationListener<ApplicationStartedEvent>, ApplicationContextAware {

    private ApplicationContext ctx;

    public static Map<String, DataSource> dataSourceHolder;

    public static final String DEFAULT_DATASOURCE_NAME = "dataSource";

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        if (ctx.getParent() == null) {
            return;
        }
        dataSourceHolder = ctx.getBeansOfType(DataSource.class);
    }

    public static DataSource getDataSource(String name) {
        DataSource dataSource;
        if (name == null || name.isEmpty()) {
            dataSource = dataSourceHolder.get(DEFAULT_DATASOURCE_NAME);
        } else {
            dataSource = dataSourceHolder.get(name);
        }
        return dataSource;
    }
}
