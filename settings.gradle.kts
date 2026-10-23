pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.fabricmc.net/")
    }
    plugins {
        id("net.neoforged.moddev") version "2.0.147"
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.8"
    id("dev.kikugie.loom-back-compat") version "0.4.2"
}

rootProject.name = stonecutter.properties["mod.id"]

stonecutter {
    create(rootProject) {
        val targets = stonecutter.properties.raw("targets").asList()
        val projects = mutableSetOf<String>()
        targets.forEach { target ->
            val values = target.asMap()
            val minecraft = values.getValue("minecraft").asPrimitive().content.toString()
            values.getValue("loaders").asList().forEach { loaderValue ->
                val loader = loaderValue.asPrimitive().content.toString()
                require(loader in setOf("fabric", "neoforge")) { "Unsupported loader '$loader' for Minecraft $minecraft" }
                val projectName = "$minecraft-$loader"
                require(projects.add(projectName)) { "Duplicate target '$projectName'" }
                version(projectName, minecraft).buildscript("build.$loader.gradle.kts")
            }
        }
        require(projects.isNotEmpty()) { "At least one Minecraft target is required" }
        vcsVersion = projects.first()
    }
}
