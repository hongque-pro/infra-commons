package com.labijie.infra.logging.test;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.labijie.infra.json.JacksonHelper;
import com.labijie.infra.logging.pojo.LogOuterClass;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class Test {

    public static void main(String[] args) throws IOException {
        Log log = new Log();
        log.setLevel("WARN");
        log.setLogger("test");
        log.setThread("main");
        log.setTimestamp(System.currentTimeMillis());
        log.setMsg("The APR based Apache Tomcat Native library which allows optimal performance in production environments was not found on the java.library.path: [/Users/gaopeng/Library/Java/Extensions:/Library/Java/Extensions:/Network/Library/Java/Extensions:/System/Library/Java/Extensions:/usr/lib/java:.]");
        log.setProject("test");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
        outputStream.writeObject(log);
        byte[] javaBytes = byteArrayOutputStream.toByteArray();
        outputStream.close();
        System.out.println("java: " + javaBytes.length);

        LogOuterClass.Log.Builder logBuilder = LogOuterClass.Log.newBuilder();
        logBuilder.setLevel(log.getLevel());
        logBuilder.setTimestamp(log.getTimestamp());
        logBuilder.setLogger(log.getLogger());
        logBuilder.setThread(log.getThread());
        logBuilder.setMessage(log.getMsg());
        logBuilder.setProject(log.getProject());
        byte[] protobufBytes = logBuilder.build().toByteArray();
        System.out.println("protobuf: " + protobufBytes.length);

        byte[] jsonBytes = JacksonHelper.INSTANCE.serialize(log, false);
        System.out.println("json: " + jsonBytes.length);

        Kryo kryo = new Kryo();
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        Output output = new Output(bs);
        kryo.writeObject(output, log);
        output.close();
        byte[] kryoBytes = bs.toByteArray();
        System.out.println("kryo: " + kryoBytes.length);
    }
}
