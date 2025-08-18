plugins {
    alias(libs.plugins.fabric.loom)
}

repositories {
    maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }
    maven { url = uri("https://repo.codemc.io/repository/maven-snapshots/") }
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    modImplementation(libs.serverevents)
    implementation(project(":realinvisibility-common"))
}

loom {
    splitEnvironmentSourceSets()
    mods {
        register("realinvisibility") {
            sourceSet(sourceSets.main.get())
        }
    }
}

tasks.processResources {
    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "version" to project.version,
                "minecraft_version" to libs.versions.minecraft.get(),
                "loader_version" to libs.versions.fabric.loader.get(),
                "serverevents_version" to libs.versions.serverevents.get()
            )
        )
    }
}

tasks.jar {
    from(project(":realinvisibility-common").sourceSets.main.get().output)
    from(rootProject.file("LICENSE")) {
        rename { "${it}_realinvisibility" }
    }
}