
dependencies {
    api(project(":core"))
    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-starter-aop")

    compileOnly("org.apache.curator:curator-recipes") {
        //excludegroup: 'org.apache.zookeeper', module: 'zookeeper'
    }
    compileOnly("org.apache.zookeeper:zookeeper"){
        exclude("slf4j-log4j12")
        exclude("log4j")
    }

    compileOnly("io.lettuce:lettuce-core")

    compileOnly("com.labijie.orm:exposed-springboot-starter:${Versions.infraOrm}")

    testImplementation("com.labijie.orm:exposed-springboot-test-starter:${Versions.infraOrm}")
}



//tasks.named("compileKotlin") {
//    inputs.files(tasks.named("processResources"))
//}
//
