plugins {
    id("com.labijie.infra") version (Versions.infraPlugin) apply false
    id("com.google.devtools.ksp") version Versions.ksp apply false
}

allprojects {
    group = "com.labijie.infra"
    version = "2.2.0"

    this.apply(plugin = "com.labijie.infra")

    infra {
        useDefault {
            includeSource = true
            infraBomVersion = Versions.infraBom
            kotlinVersion = Versions.kotlin
            useMavenProxy = true
            useMavenProxy = false
        }

        useNexusPublish()
    }
}

subprojects {
    infra {
        if (!project.name.startsWith("dummy")) {
            usePublish {
                description = "infrastructure library"
                githubUrl("hongque-pro", "infra-commons")
                artifactId {
                    val n = if (it.name == "core") "commons" else "commons-${it.name}"
                    print("${it.name} --> $n")
                    n
                }
            }
        }
    }
}
