pluginManagement {
    repositories {
        maven {
            name = "FabricMC"
            url = uri("https://maven.fabricmc.net/")
        }
        maven("https://repo.papermc.io/repository/maven-public/")
        mavenCentral()
        gradlePluginPortal()
    }
}

include("realinvisibility-common", "realinvisibility-fabric", "realinvisibility-paper")
