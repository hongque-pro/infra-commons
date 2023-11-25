
dependencies {
    api(project(":springboot-starter"))
    api("com.h2database:h2")
    api("com.labijie.orm:exposed-starter:${Versions.infraOrm}")
    api("org.springframework.boot:spring-boot-starter-web")
}