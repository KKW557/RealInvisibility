plugins {
    alias(libs.plugins.fabric.loom)
}

repositories {
    maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }
    maven { url = uri("https://repo.codemc.io/repository/maven-snapshots/") }
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
}