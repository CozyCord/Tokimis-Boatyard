import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.file.RelativePath
import groovy.json.JsonGenerator

plugins {
    id("dev.kikugie.loom-back-compat")
    `maven-publish`
}

val target = extensions.getByType(StonecutterBuildExtension::class.java)
val minecraftVersion = target.current.version
val javaVersion = project.property("java.version").toString().toInt()
val jeiVersion = project.property("jei_version").toString()
val modVersion = "${project.property("mod.version")}+$minecraftVersion-fabric"
val jsonGenerator = JsonGenerator.Options().disableUnicodeEscaping().build()
val escaped = { value: String -> jsonGenerator.toJson(value).toString().removeSurrounding("\"") }
val modId = project.property("mod.id").toString()
val modName = escaped(project.property("mod.name").toString())
val authors = target.properties.raw("mod", "authors").asList().map { it.asPrimitive().content.toString() }
val authorsJson = jsonGenerator.toJson(authors)
val escapedDescription = escaped(project.property("mod.description").toString())
val escapedLicense = escaped(project.property("mod.license").toString())

group = project.property("mod.group") as String
version = modVersion
base.archivesName = project.property("mod.id") as String

repositories {
    maven("https://maven.fabricmc.net/")
    maven("https://maven.blamejared.com/")
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    loomx.applyMojangMappings()
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
    modCompileOnly("mezz.jei:jei-${minecraftVersion}-common-api-intermediary:$jeiVersion") {
        isTransitive = false
    }
}

tasks.processResources {
    inputs.properties(mapOf("version" to project.version, "minecraftVersion" to minecraftVersion, "javaVersion" to javaVersion, "description" to escapedDescription, "modId" to project.property("mod.id"), "modName" to escaped(project.property("mod.name").toString()), "authors" to authorsJson, "license" to escapedLicense))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("META-INF/neoforge.mods.toml")
    exclude("tokimiboatyard.neoforge.mixins.json")
    filesMatching("fabric.mod.json") {
        expand(
            "version" to modVersion,
            "minecraft_version" to "~$minecraftVersion",
            "java_version" to ">=$javaVersion",
            "java_level" to javaVersion,
            "description" to escapedDescription,
            "modId" to modId,
            "modName" to modName,
            "license" to escapedLicense
        )
        filter(
            mapOf("tokens" to mapOf("authors" to authorsJson), "beginToken" to "[\"@", "endToken" to "@\"]"),
            org.apache.tools.ant.filters.ReplaceTokens::class.java
        )
    }
    filesMatching("tokimiboatyard.mixins.json") {
        expand("java_level" to javaVersion)
    }
    eachFile {
        if (minecraftVersion == "1.21.1" && path.startsWith("data/tokimiboatyard/recipes/")) {
            relativePath = RelativePath(true, *path.replaceFirst("/recipes/", "/recipe/").split('/').toTypedArray())
        }
    }
}

extensions.configure<JavaPluginExtension> {
    withSourcesJar()
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(javaVersion)
}

tasks.withType<Jar>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("META-INF/neoforge.mods.toml")
    exclude("tokimiboatyard.neoforge.mixins.json")
}

tasks.jar {
    inputs.property("archivesName", project.base.archivesName)
    from(rootProject.file("LICENSE")) {
        rename { "${it}_${inputs.properties["archivesName"]}" }
    }
}

loom {
    runConfigs.configureEach {
        preferGradleTask = true
        runDirectory = rootProject.file("run")
    }
}
