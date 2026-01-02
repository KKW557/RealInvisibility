group = "icu.suc"
version = "2026.1.1"

subprojects {
    plugins.apply("java")

    project.version = rootProject.version

    configure<JavaPluginExtension> {
        withSourcesJar()
    }
}