plugins {
    id("dev.kikugie.loom-back-compat")
    `maven-publish`
}

val minecraftVersion = stonecutter.current.version
val modId = project.property("mod.id") as String
val modVersion = "${project.property("mod.version")}+$minecraftVersion-fabric"
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

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    loomx.applyMojangMappings()
    modImplementation("net.fabricmc:fabric-loader:${project.property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("deps.fabric_api")}")
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")
    mods {
        create(modId) {
            sourceSet(sourceSets.main.get())
        }
    }
    runConfigs.configureEach { runDirectory = rootProject.file("run") }
}


tasks.processResources {
    val metadata = mapOf("version" to modVersion, "modId" to modId, "modName" to project.property("mod.name"), "description" to project.property("mod.description"), "license" to project.property("mod.license"), "homepage" to project.property("mod.homepage"), "issues" to project.property("mod.issues"), "sources" to project.property("mod.sources"), "fabricLoader" to project.property("deps.fabric_loader"), "minecraftRange" to project.property("mod.fabric_mc_range"), "javaVersion" to javaVersion, "fabricApi" to project.property("deps.fabric_api"))
    inputs.properties(metadata)
    filesMatching("fabric.mod.json") { expand(metadata) }
    exclude("META-INF/neoforge.mods.toml")
}

tasks.named<Jar>("sourcesJar") { exclude("META-INF/neoforge.mods.toml") }

publishing {
    publications {
        create<MavenPublication>("mavenJava") { from(components["java"]) }
    }
}
