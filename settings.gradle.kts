plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "modb-app"
include("analyzer")
include("anidb")
include("anilist")
include("anisearch")
include("app")
include("lib")
