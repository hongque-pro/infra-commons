rootProject.name = "commons"
include("core")
include("core-starter")
include("snowflake-starter")
include("sink-core")
include("sink-elasticsearch")
include("sink-json-starter")
include("kafka-starter")
include("distribution-starter")
include("dummy-server")
include("logging")
include("jpa")
include("logging-starter")
include("mybatis-starter")
include("sink-elasticsearch-rest")
include("mybatis-dynamic-starter")

pluginManagement {

    repositories {
        mavenLocal()

        gradlePluginPortal()
    }
}

