
plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    api(project(":core-starter"))

    implementation("io.lettuce:lettuce-core")

    implementation("org.apache.curator:curator-framework") {
        exclude("org.apache.zookeeper", "zookeeper")
    }
    implementation("org.apache.zookeeper:zookeeper") {
        exclude(module = "slf4j-log4j12")
        exclude(module = "log4j")
    }

    compileOnly("com.labijie.orm:exposed-starter:${Versions.infraOrm}")

    ksp("com.labijie.orm:exposed-generator:${Versions.infraOrm}")

    testImplementation("com.labijie.orm:exposed-test-starter:${Versions.infraOrm}")
}