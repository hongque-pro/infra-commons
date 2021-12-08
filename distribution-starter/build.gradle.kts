
dependencies {
    api(project(":core-starter"))
    api("org.springframework.boot:spring-boot-starter-aop")

    implementation("org.apache.curator:curator-recipes") {
        //excludegroup: 'org.apache.zookeeper', module: 'zookeeper'
    }
    implementation("org.apache.zookeeper:zookeeper"){
//        exclude module: "slf4j-log4j12"
//        exclude module: "log4j"
    }
}
