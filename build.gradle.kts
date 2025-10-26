group = "icu.suc"
version = "2.1.7"

subprojects {
    plugins.apply("java")

    project.version = rootProject.version

    configure<JavaPluginExtension> {
        withSourcesJar()
    }
}