package com.labijie.infra.commons.sink.sink;

public class SinkStoreException extends Exception {
    public SinkStoreException(){
        super();
    }

    public SinkStoreException(String error, Throwable cause){
        super(error, cause);
    }

    public SinkStoreException(String error){
        super(error);
    }
}
