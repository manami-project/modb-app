import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kover)
    `maven-publish`
    `java-library`
}

group = "io.github.manamiproject"
version = project.findProperty("release.version") as String? ?: ""

val githubUsername = "manami-project"
val kotlinVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_3 // most recent stable kotlin version for language and std lib

repositories {
    mavenCentral()
    maven {
        name = "kommand"
        url = uri("https://maven.pkg.github.com/$githubUsername/kommand")
        credentials {
            username = parameter("GH_USERNAME", githubUsername)
            password = parameter("GH_PACKAGES_READ_TOKEN")
        }
    }
}

dependencies {
    api(libs.kotlin.stdlib)
    api(project(":anidb"))
    api(project(":anilist"))
    api(project(":anime-planet"))
    api(project(":animenewsnetwork"))
    api(project(":anisearch"))
    api(project(":core"))
    api(project(":kitsu"))
    api(project(":livechart"))
    api(project(":myanimelist"))
    api(project(":notify"))
    api(project(":serde"))
    api(project(":simkl"))

    implementation(libs.kommand)
    implementation(libs.logback.classic)
    implementation(libs.commons.text)

    testImplementation(libs.kotlin.reflect)
    testImplementation(project(":test"))
}

kotlin {
    jvmToolchain(JavaVersion.VERSION_25.toString().toInt())
}

kover {
    reports {
        filters {
            excludes {
                annotatedBy("io.github.manamiproject.modb.core.coverage.KoverIgnore")
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_25)
        apiVersion.set(kotlinVersion)
        languageVersion.set(kotlinVersion)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    reports.html.required.set(false)
    reports.junitXml.required.set(true)
    maxParallelForks = rootProject.extra["maxParallelForks"] as Int
    systemProperty("junit.jupiter.execution.parallel.enabled", "true")
    systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javaDoc by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(sourceSets.main.get().allSource)
}

fun parameter(name: String, default: String = ""): String {
    val env = System.getenv(name) ?: ""
    if (env.isNotBlank()) {
        return env
    }

    val property = project.findProperty(name) as String? ?: ""
    if (property.isNotBlank()) {
        return property
    }

    return default
}
