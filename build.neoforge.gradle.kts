plugins {
    id("net.neoforged.moddev")
    id("me.modmuss50.mod-publish-plugin")
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

val modrinthId = project.property("publish.modrinth").toString().trim()
val curseforgeId = project.property("publish.curseforge").toString().trim()
val modrinthToken = providers.gradleProperty("MODRINTH_TOKEN").filter { it.isNotBlank() }.orElse(providers.environmentVariable("MODRINTH_TOKEN").filter { it.isNotBlank() })
val curseforgeToken = providers.gradleProperty("CURSEFORGE_TOKEN").filter { it.isNotBlank() }.orElse(providers.environmentVariable("CURSEFORGE_TOKEN").filter { it.isNotBlank() })
val publishDryRun = providers.gradleProperty("publish.dry_run").orElse("false").map {
    when (it) {
        "true" -> true
        "false" -> false
        else -> throw GradleException("publish.dry_run must be true or false")
    }
}
val releaseType = when (project.property("publish.release_type").toString().lowercase()) {
    "stable" -> me.modmuss50.mpp.ReleaseType.STABLE
    "beta" -> me.modmuss50.mpp.ReleaseType.BETA
    "alpha" -> me.modmuss50.mpp.ReleaseType.ALPHA
    else -> throw GradleException("publish.release_type must be stable, beta, or alpha")
}
val publishEnvironment = me.modmuss50.mpp.platforms.modrinth.ModrinthEnvironment.CLIENT_AND_SERVER

publishMods {
    file = tasks.named<Jar>("jar").flatMap { it.archiveFile }
    dryRun.set(publishDryRun)
    version = modVersion
    displayName = "${project.property("mod.name")} ${project.property("mod.version")} - NeoForge ${minecraftVersion}"
    changelog = providers.fileContents(rootProject.layout.projectDirectory.file("CHANGELOG.md")).asText
    type = releaseType
    modLoaders.add("neoforge")

    if (modrinthId.isNotBlank()) modrinth {
        projectId = modrinthId
        accessToken.set(modrinthToken)
        minecraftVersions.add(minecraftVersion)
        environment.set(publishEnvironment)
        additionalFile(tasks.named<Jar>("sourcesJar").flatMap { it.archiveFile }) { type = me.modmuss50.mpp.platforms.modrinth.ModrinthApi.AdditionalFileType.SOURCES_JAR }
    }
    if (curseforgeId.isNotBlank()) curseforge {
        projectId = curseforgeId
        accessToken.set(curseforgeToken)
        minecraftVersions.add(minecraftVersion)
        client = true
        server = true
        additionalFile(tasks.named<Jar>("sourcesJar").flatMap { it.archiveFile }) { name = "Sources" }
    }
}
