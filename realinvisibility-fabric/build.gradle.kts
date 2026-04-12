plugins {
    alias(libs.plugins.fabric.loom)
}

repositories {
    maven("https://mvn.suc.icu")
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    modImplementation(libs.serverevents)
    implementation(project(":realinvisibility-common"))
}

tasks.processResources {
    filteringCharset = "UTF-8"

    inputs.property("version", project.version)
    inputs.property("minecraft", libs.versions.minecraft.get())
    inputs.property("loader", libs.versions.fabric.loader.get())
    inputs.property("serverevents", libs.versions.serverevents.get())
    inputs.property("api", libs.versions.fabric.api.get())

    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "version" to project.version,
                "minecraft" to libs.versions.minecraft.get(),
                "loader" to libs.versions.fabric.loader.get(),
                "serverevents" to libs.versions.serverevents.get(),
                "api" to libs.versions.fabric.api.get()
            )
        )
    }
}

tasks.jar {
    from(project(":realinvisibility-common").sourceSets.main.get().output)
}