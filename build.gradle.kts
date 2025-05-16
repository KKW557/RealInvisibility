import java.util.Locale

group = "icu.suc"
version = "2.1.0"

subprojects {
    plugins.apply("java")

    project.version = rootProject.version
}

tasks.register("build") {
    group = "build"

    dependsOn(subprojects.mapNotNull { it.tasks.findByName("build") })

    doLast {
        val rootLibs = layout.buildDirectory.dir("libs").get().asFile
        rootLibs.mkdirs()

        subprojects.forEach { subproject ->
            subproject.layout.buildDirectory.dir("libs").get().asFile.listFiles()?.forEach { jar ->
                val newName = "${rootProject.name}-${jar.name}"
                val target = rootLibs.resolve(newName)
                jar.copyTo(target, overwrite = true)
            }
        }
    }
}

tasks.register<Delete>("clean") {
    group = "build"

    delete(layout.buildDirectory)
}