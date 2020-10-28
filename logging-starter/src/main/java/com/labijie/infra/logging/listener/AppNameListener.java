package com.labijie.infra.logging.listener;

import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

public class AppNameListener implements ApplicationListener<ApplicationPreparedEvent>, ApplicationContextAware {

    private ApplicationContext ctx;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        Environment environment = ctx.getEnvironment();
        String appName = environment.getProperty("spring.application.name");
        System.setProperty("spring.application.name", appName);
    }
}
