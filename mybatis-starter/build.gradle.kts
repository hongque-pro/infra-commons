
dependencies {
    api(project(":core-starter"))
    api("org.mybatis.spring.boot:mybatis-spring-boot-starter") {
//        exclude module:'spring-boot-starter-jdbc'
//        exclude module:'mybatis'
    }
    api("org.springframework.boot:spring-boot-starter-jdbc") {
        //exclude module:'jsqlparser'
    }

    api("org.mybatis:mybatis")
    api("com.github.jsqlparser:jsqlparser")
    api("com.github.pagehelper:pagehelper")
    api("org.mybatis.generator:mybatis-generator-core")

}