
dependencies {
    implementation("org.apache.kafka:kafka-clients")
    api("ch.qos.logback:logback-classic")
    api("ch.qos.logback:logback-core")
    api("com.google.protobuf:protobuf-java")
    api(project(":core"))

    testImplementation("com.esotericsoftware:kryo")

}

tasks.create("compileProtocolBuffer", Exec::class) {
    commandLine("protoc", "--java_out=src/main/java/", "idl/log.proto")
}
