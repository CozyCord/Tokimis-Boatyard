import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.api.file.RelativePath
import groovy.json.JsonGenerator

plugins {
    id("net.neoforged.moddev")
}

val target = extensions.getByType(StonecutterBuildExtension::class.java)
val minecraftVersion = target.current.version
val javaVersion = project.property("java.version").toString().toInt()
val neoForgeVersion = project.property("deps.neoforge").toString()
val jeiVersion = project.property("jei_version").toString()
val modId = project.property("mod.id").toString()
val modName = project.property("mod.name").toString()
val jsonGenerator = JsonGenerator.Options().disableUnicodeEscaping().build()
val escaped = { value: String -> jsonGenerator.toJson(value).toString().removeSurrounding("\"") }
val authors = target.properties.raw("mod", "authors").asList().map { it.asPrimitive().content.toString() }
val escapedDescription = escaped(project.property("mod.description").toString())
val escapedAuthors = escaped(authors.joinToString(", "))
val modVersion = "${project.property("mod.version")}+$minecraftVersion-neoforge"
val mainSourceSet = extensions.getByType<SourceSetContainer>().named("main").get()

group = project.property("mod.group") as String
version = modVersion
base.archivesName = modId

repositories {
    maven("https://maven.blamejared.com/")
    mavenCentral()
}

dependencies {
    compileOnly("mezz.jei:jei-${minecraftVersion}-neoforge-api:${project.property("jei_version")}")
}

extensions.configure<JavaPluginExtension> {
    withSourcesJar()
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(javaVersion)
}

val metadata = mapOf(
    "version" to modVersion,
    "modIdKey" to modId,
    "modName" to escaped(modName),
    "authors" to escapedAuthors,
    "description" to escapedDescription,
    "license" to escaped(project.property("mod.license").toString()),
    "java" to javaVersion.toString(),
    "java_level" to javaVersion.toString(),
    "mc" to "[$minecraftVersion]",
    "neoforge" to neoForgeVersion,
    "jei" to jeiVersion
)

tasks.named<ProcessResources>("processResources") {
    inputs.properties(metadata)
    exclude("fabric.mod.json")
    filesMatching("META-INF/neoforge.mods.toml") { expand(metadata) }
    filesMatching("tokimiboatyard.mixins.json") { expand(metadata) }
    eachFile {
        if (minecraftVersion == "1.21.1" && path.startsWith("data/tokimiboatyard/recipes/")) {
            relativePath = RelativePath(true, *path.replaceFirst("/recipes/", "/recipe/").split('/').toTypedArray())
        }
    }
}

tasks.named<Jar>("sourcesJar") { exclude("fabric.mod.json") }

neoForge {
    version = neoForgeVersion
    runs {
        create("client") { client(); gameDirectory = rootProject.file("run/neoforge") }
        create("server") { server(); gameDirectory = rootProject.file("run/neoforge") }
    }
    mods.create(modId) { sourceSet(mainSourceSet) }
}
