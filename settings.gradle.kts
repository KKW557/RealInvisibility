pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven {
            name = "FabricMC"
            url = uri("https://maven.fabricmc.net/")
        }
    }
}

include(
    "realinvisibility-common",
    "realinvisibility-paper",
    "realinvisibility-fabric"
)
