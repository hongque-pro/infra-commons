plugins {
    id("com.labijie.infra") version (Versions.infraPlugin) apply false
}


allprojects {
    group = "com.labijie.infra"
    version = Versions.projectVersion

    this.apply(plugin = "com.labijie.infra")
    infra {
        useDefault {
            includeSource = true
            infraBomVersion = Versions.infraBom
            kotlinVersion = Versions.kotlin
        }

        //useNexusPublish()
    }
}

subprojects {
    infra {
        if (!project.name.startsWith("dummy")) {
            publishing {
                pom {
                    description = "infrastructure library"
                    githubUrl("hongque-pro", "infra-commons")
                    artifactId {
                        val n = "commons-${it.name}"
                        print("${it.name} --> $n")
                        n
                    }
                }
            }
        }
    }
}
