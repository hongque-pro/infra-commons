plugins {
    id("com.labijie.infra") version(Versions.infraPlugin) apply false
    id("com.google.devtools.ksp") version Versions.ksp apply false
}

allprojects {
    this.apply(plugin="com.labijie.infra")

    infra {
        useDefault {
            infraBomVersion = Versions.infraBom
            kotlinVersion = Versions.kotlin
            useMavenProxy = true
        }

        if(!project.name.startsWith("dummy")){
            usePublish {
                description="infrastructure library"
                projectUrl="https://github.com/hongque-pro/infra-commons"
                githubScmUrl="scm:git@github.com:hongque-pro/infra-commons.git"
                gitUrl="https://github.com/hongque-pro/infra-commons.git"
            }
        }

        useNexusPublish()
    }
}
