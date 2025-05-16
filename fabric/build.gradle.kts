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
    implementation(project(":common"))
    include(project(":common"))
}

loom {
    splitEnvironmentSourceSets()
    mods {
        register("realinvisibility") {
            sourceSet(sourceSets.main.get())
        }
    }
    mixin {
        defaultRefmapName = "realinvisibility-refmap.json"
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