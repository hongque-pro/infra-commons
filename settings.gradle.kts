rootProject.name = "commons"
include("core")
include("dummy-server")
include("mybatis-starter")

pluginManagement {

    repositories {
        mavenLocal()

        gradlePluginPortal()
    }
}
include("springboot-starter")

