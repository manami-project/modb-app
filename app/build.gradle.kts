import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kover)
    alias(libs.plugins.shadow)
    application
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
    implementation(libs.kotlin.stdlib)
    implementation(project(":lib"))
    implementation(libs.logback.classic)

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

val mainClassPath = "io.github.manamiproject.modb.app.AppKt"
application {
    mainClass.set(mainClassPath)
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("modb-app")
        archiveClassifier.set("")
        archiveVersion.set("")
        manifest {
            attributes["Main-Class"] = mainClassPath
        }
        exclude(".gitemptydir")
    }
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
