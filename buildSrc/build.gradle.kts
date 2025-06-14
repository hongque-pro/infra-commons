plugins {
    `kotlin-dsl`
}

fun getProxyMavenRepository(): String? {
    return System.getenv("MAVEN_PROXY")?.ifBlank { null }
}

repositories {
    mavenLocal()
    gradlePluginPortal()
    getProxyMavenRepository()?.let {
        maven {
            this.setUrl(it)
            this.isAllowInsecureProtocol = true
        }
    }
    mavenCentral()
}
