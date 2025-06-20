
infra {
    useInfraOrmGenerator(Versions.infraOrm)
}


dependencies {
    api(project(":core"))
    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-starter-aop")
    api("org.springframework.boot:spring-boot-starter-web")

    compileOnly("io.etcd:jetcd-core:${Versions.jetcd}") {
        exclude(group = "com.google.guava")
        exclude(group = "org.slf4j")
    }

    compileOnly("org.apache.curator:curator-recipes")

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
