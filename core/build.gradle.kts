dependencies {
    // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    api("ch.qos.logback:logback-classic")
    api("org.slf4j:log4j-over-slf4j")
    api("com.fasterxml.jackson.module:jackson-module-kotlin"){
       exclude(module = "slf4j-log4j12")
       exclude(module="kotlin-reflect")
    }
    api("io.netty:netty-common")
    compileOnly("com.esotericsoftware:kryo")
    // https://mvnrepository.com/artifact/com.cronutils/cron-utils
    api("com.cronutils:cron-utils")

    api("commons-validator:commons-validator:1.7")

    api("org.msgpack:jackson-dataformat-msgpack"){
        exclude(module="slf4j-log4j12")
    }
}