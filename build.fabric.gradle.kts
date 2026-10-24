plugins {
    id("dev.kikugie.loom-back-compat")
    id("me.modmuss50.mod-publish-plugin")
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
val publishEnvironment = when (project.property("environment").toString()) {
    "client" -> me.modmuss50.mpp.platforms.modrinth.ModrinthEnvironment.CLIENT_ONLY
    "server" -> me.modmuss50.mpp.platforms.modrinth.ModrinthEnvironment.SERVER_ONLY
    "*" -> me.modmuss50.mpp.platforms.modrinth.ModrinthEnvironment.CLIENT_AND_SERVER
    else -> throw GradleException("environment must be client, server, or *")
}
val modrinthRequires = stonecutter.properties.rawOrNull("publish.modrinth_requires")?.asList().orEmpty().map { it.asPrimitive().content.toString() }
val curseforgeRequires = stonecutter.properties.rawOrNull("publish.curseforge_requires")?.asList().orEmpty().map { it.asPrimitive().content.toString() }

publishMods {
    file = loomx.modJar.flatMap { it.archiveFile }
    dryRun.set(publishDryRun)
    version = modVersion
    displayName = "${project.property("mod.name")} ${project.property("mod.version")} - Fabric ${minecraftVersion}"
    changelog = providers.fileContents(rootProject.layout.projectDirectory.file("CHANGELOG.md")).asText
    type = releaseType
    modLoaders.add("fabric")

    if (modrinthId.isNotBlank()) modrinth {
        projectId = modrinthId
        accessToken.set(modrinthToken)
        minecraftVersions.add(minecraftVersion)
        environment.set(publishEnvironment)
        modrinthRequires.forEach { requires(it) }
        additionalFile(loomx.modSourcesJar.flatMap { it.archiveFile }) { type = me.modmuss50.mpp.platforms.modrinth.ModrinthApi.AdditionalFileType.SOURCES_JAR }
    }
    if (curseforgeId.isNotBlank()) curseforge {
        projectId = curseforgeId
        accessToken.set(curseforgeToken)
        minecraftVersions.add(minecraftVersion)
        client = publishEnvironment != me.modmuss50.mpp.platforms.modrinth.ModrinthEnvironment.SERVER_ONLY
        server = publishEnvironment != me.modmuss50.mpp.platforms.modrinth.ModrinthEnvironment.CLIENT_ONLY
        curseforgeRequires.forEach { requires(it) }
        additionalFile(loomx.modSourcesJar.flatMap { it.archiveFile }) { name = "Sources" }
    }
}
