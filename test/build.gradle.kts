import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kover)
    `maven-publish`
    `java-library`
}

group = "io.github.manamiproject"
version = project.findProperty("release.version") as String? ?: ""

val projectName = "modb-test"
val githubUsername = "manami-project"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.kotlin.stdlib)
    api(libs.kotlin.test.junit5)
    api(libs.junit.jupiter.engine)
    api(libs.junit.jupiter.params)
    api(libs.junit.platform.launcher)
    api(libs.assertj.core)
    api(libs.wiremock)
    api(libs.kotlinx.coroutines.core.jvm)
}

kotlin {
    explicitApi()
    jvmToolchain(JavaVersion.VERSION_21.toString().toInt())
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

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javaDoc by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(sourceSets.main.get().allSource)
}

publishing {
    repositories {
        maven {
            name = projectName
            url = uri("https://maven.pkg.github.com/$githubUsername/$projectName")
            credentials {
                username = parameter("GH_USERNAME", githubUsername)
                password = parameter("GH_PACKAGES_RELEASE_TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = projectName
            version = project.version.toString()

            from(components["java"])
            artifact(sourcesJar.get())
            artifact(javaDoc.get())

            pom {
                packaging = "jar"
                name.set(projectName)
                description.set("This lib contains all essential dependencies as well as some convenience functions and classes for creating tests in modb prefixed kotlin projects.")
                url.set("https://github.com/$githubUsername/$projectName")

                licenses {
                    license {
                        name.set("AGPL-V3")
                        url.set("https://www.gnu.org/licenses/agpl-3.0.txt")
                    }
                }

                scm {
                    connection.set("scm:git@github.com:$githubUsername/$projectName.git")
                    developerConnection.set("scm:git:ssh://github.com:$githubUsername/$projectName.git")
                    url.set("https://github.com/$githubUsername/$projectName")
                }
            }
        }
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