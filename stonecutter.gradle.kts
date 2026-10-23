plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.20.1-fabric"

stonecutter.parameters {
    val loader = current.project.substringAfterLast('-')
    properties { tags(current.version, loader) }
    constants.match(loader, "fabric", "neoforge")
}

