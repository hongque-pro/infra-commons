
dependencies{
    api(project(":core-starter"))
    api ("org.mybatis.spring.boot:mybatis-spring-boot-starter"){
//        exclude module:'spring-boot-starter-jdbc'
//        exclude module:'mybatis'
    }
    api ("org.springframework.boot:spring-boot-starter-jdbc")

    api("org.mybatis:mybatis")
    api("org.mybatis.dynamic-sql:mybatis-dynamic-sql")

}