val minecraft = libs.versions.minecraft.get()

val targetJavaVersion = 21

subprojects {
    project.version = "${project.version}+${minecraft}"

    plugins.withId("java") {
        extensions.configure<JavaPluginExtension> {
            toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
            withSourcesJar()
            withJavadocJar()
        }

        tasks.withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
            options.release.set(targetJavaVersion)
        }

        tasks.withType<Jar>().configureEach {
            from(rootProject.file("LICENSE")) {
                into("META-INF")
            }
        }

        apply(plugin = "maven-publish")
        extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("mavenJava") {
                    from(components["java"])
                }
            }

            repositories {
                maven { url = uri(rootProject.layout.buildDirectory.dir("repository")) }
            }
        }
    }
}