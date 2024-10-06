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

repositories {
    mavenCentral()
    maven {
        name = "modb-core"
        url = uri("https://maven.pkg.github.com/$githubUsername/modb-core")
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

    testImplementation(libs.modb.test)
}

kotlin {
    jvmToolchain(JavaVersion.VERSION_21.toString().toInt())
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
        jvmTarget.set(JvmTarget.JVM_21)
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    reports.html.required.set(false)
    reports.junitXml.required.set(true)
    maxParallelForks = Runtime.getRuntime().availableProcessors()
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
    if (property.isNotEmpty()) {
        return property
    }

    return default
}
