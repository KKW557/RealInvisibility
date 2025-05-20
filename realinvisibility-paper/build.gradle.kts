plugins {
    alias(libs.plugins.paperweight)
}

repositories {
    maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }
    maven { url = uri("https://repo.codemc.io/repository/maven-snapshots/") }
}

dependencies {
    paperweight.paperDevBundle(libs.versions.userdev)
    implementation(libs.packetevents.spigot)
    implementation(project(":realinvisibility-common"))
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand(
            mapOf(
                "version" to project.version,
            )
        )
    }
}

tasks.jar {
    from(project(":realinvisibility-common").sourceSets.main.get().output)
}