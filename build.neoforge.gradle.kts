plugins {
    id("net.neoforged.moddev")
    `maven-publish`
}

val minecraftVersion = stonecutter.current.version
val modId = project.property("mod.id") as String
val modVersion = "${project.property("mod.version")}+$minecraftVersion-neoforge"
val modGroup = project.property("mod.group") as String
val javaVersion = (project.property("java.version") as String).toInt()

version = modVersion
group = modGroup
base.archivesName = modId

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(javaVersion)
}

tasks.processResources {
    val metadata = mapOf("version" to modVersion, "modId" to modId, "modName" to project.property("mod.name"), "description" to project.property("mod.description"), "license" to project.property("mod.license"), "homepage" to project.property("mod.homepage"), "issues" to project.property("mod.issues"), "sources" to project.property("mod.sources"), "mc" to project.property("mod.neoforge_mc_range"), "neoforge" to project.property("deps.neoforge"))
    inputs.properties(metadata)
    filesMatching("META-INF/neoforge.mods.toml") { expand(metadata) }
    exclude("fabric.mod.json")
}


neoForge {
    version = project.property("deps.neoforge") as String
    runs {
        create("client") { client(); gameDirectory = rootProject.file("run") }
        create("server") { server(); gameDirectory = rootProject.file("run") }
    }
    mods.create(modId) { sourceSet(sourceSets.main.get()) }
}

tasks.named<Jar>("sourcesJar") { exclude("fabric.mod.json") }

publishing {
    publications {
        create<MavenPublication>("mavenJava") { from(components["java"]) }
    }
}
