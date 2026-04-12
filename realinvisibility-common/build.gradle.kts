plugins {
    alias(libs.plugins.fabric.loom)
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
}