

plugins {
    id("org.springframework.boot") version Versions.infraBom
    id("org.graalvm.buildtools.native") version Versions.nativeBuildVersion
}

graalvmNative {
    binaries {
        named("main"){
            sharedLibrary.set(false)
            mainClass.set("com.labijie.infra.dummy.ApplicationKt")
        }

    }
}

dependencies {
    implementation(project(":springboot-starter"))
    implementation("com.h2database:h2")
    implementation("com.labijie.orm:exposed-springboot-starter:${Versions.infraOrm}")
    implementation("org.springframework.boot:spring-boot-starter-web")
}
