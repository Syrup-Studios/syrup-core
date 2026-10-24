plugins {
    id("dev.kikugie.stonecutter")
    id("net.neoforged.moddev") version "2.0.147" apply false
    id("me.modmuss50.mod-publish-plugin") version "2.2.0" apply false
}

stonecutter active null

stonecutter.parameters {
    val loader = current.project.substringAfterLast('-')
    properties { tags(current.version, loader) }
    constants.match(loader, "fabric", "neoforge")
}
