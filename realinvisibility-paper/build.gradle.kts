plugins {
    alias(libs.plugins.paperweight.userdev)
}

repositories {
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
}

dependencies {
    paperweight.paperDevBundle(libs.versions.paperweight.bundle)
    implementation(libs.packetevents.spigot)
    implementation(project(":realinvisibility-common"))
}

tasks.processResources {
    filteringCharset = "UTF-8"

    inputs.property("version", project.version)
    inputs.property("minecraft", libs.versions.minecraft.get())

    filesMatching("paper-plugin.yml") {
        expand(
            mapOf(
                "version" to project.version,
                "minecraft" to libs.versions.minecraft.get()
            )
        )
    }
}

tasks.jar {
    from(project(":realinvisibility-common").sourceSets.main.get().output)
}