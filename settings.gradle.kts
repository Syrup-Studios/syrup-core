pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.fabricmc.net/")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.8"
    id("dev.kikugie.loom-back-compat") version "0.4.2"
}

rootProject.name = stonecutter.properties["mod.id"]

stonecutter {
    create(rootProject) {
        stonecutter.properties.raw("targets").asList().forEach { target ->
            val values = target.asMap()
            val minecraft = values.getValue("minecraft").asPrimitive().content.toString()
            values.getValue("loaders").asList().forEach { loader ->
                val name = loader.asPrimitive().content.toString()
                version("$minecraft-$name", minecraft).buildscript("build.$name.gradle.kts")
            }
        }
        vcsVersion = "1.21.1-fabric"
    }
}
