plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

rootProject.name = "modb-app"
include("analyzer")
include("anidb")
include("anilist")
include("anime-planet")
include("animenewsnetwork")
include("anisearch")
include("app")
include("core")
include("kitsu")
include("lib")
include("livechart")
include("myanimelist")
include("notify")
include("serde")
include("simkl")
include("test")

val maxParallelForks = if (System.getenv("CI") == "true") {
    2
} else {
    (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(2)
}

gradle.projectsLoaded {
    rootProject.extra.set("maxParallelForks", maxParallelForks)
}
