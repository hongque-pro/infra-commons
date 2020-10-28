package com.labijie.infra.commons.sink.sink;

/**
 *  表示 Sink 组件发生配置错误。
 */
public class SinkConfigurationException extends Exception {
    public SinkConfigurationException(){
        super();
    }

    public SinkConfigurationException(String error, Throwable cause){
        super(error, cause);
    }

    public SinkConfigurationException(String error){
        super(error);
    }
}
