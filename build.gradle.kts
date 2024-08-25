plugins {
  `java-library`
  id("io.papermc.paperweight.userdev").version("1.7.2")
}

group = "icu.suc.kevin557.realinvisibility"
version = "1.0.0"

java {
  toolchain.languageVersion = JavaLanguageVersion.of(21)
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

repositories {
  mavenLocal()
  maven("https://repo.papermc.io/repository/maven-public/")
  maven("https://oss.sonatype.org/content/groups/public/")
  maven("https://repo.dmulloy2.net/repository/public/")
  mavenCentral()
}

dependencies {
  paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
  implementation("com.comphenix.protocol", "ProtocolLib", "5.3.0-SNAPSHOT")
}

tasks {
  compileJava {
    options.release = 21
  }
  javadoc {
    options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
  }
}
