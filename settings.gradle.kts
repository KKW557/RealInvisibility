pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            name = "FabricMC"
            url = uri("https://maven.fabricmc.net/")
        }
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

include(
    "realinvisibility-common",
    "realinvisibility-fabric",
    "realinvisibility-paper"
)
