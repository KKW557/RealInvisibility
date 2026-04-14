plugins {
    alias(libs.plugins.fabric.loom)
}

repositories {
    maven("https://mvn.suc.icu")
}

dependencies {
    minecraft(libs.minecraft)
    implementation(libs.fabric.loader)
    implementation(libs.fabric.api)
    implementation(libs.serverevents)
    implementation(project(":realinvisibility-common"))
    include(project(":realinvisibility-common"))
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