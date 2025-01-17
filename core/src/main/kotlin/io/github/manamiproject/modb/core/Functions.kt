package io.github.manamiproject.modb.core

import io.github.manamiproject.modb.core.config.ContextAware
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_FS
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.extensions.regularFileExists
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.net.URI
import java.nio.file.Paths
import java.security.SecureRandom
import java.util.jar.JarFile
import java.util.zip.ZipException
import kotlin.io.path.toPath

/**
 * During development: Reads the content of a file from _src/main/resources_ into a [String].
 * After build: Reads the content of a file from _*.jar_ file into a [String].
 * System specific line separators will always be converted to `\n`.
 *
 * **Example**:
 *
 * For _src/main/resources/file.txt_ you can call
 * ```
 * val content = testResource("file.txt")
 * ```
 * For _src/test/resources/dir/subdir/file.txt_ you can call
 * ```
 * val content = testResource("dir/subdir/file.txt")
 * ```
 *
 * @since 8.0.0
 * @return Content of a file as [String].
 * @throws IllegalArgumentException If the given path is blank.
 * @throws IllegalStateException If the given path does not exist.
 */
public suspend fun loadResource(path: String): String = withContext(LIMITED_FS) {
    require(path.neitherNullNorBlank()) { "Given path must not be blank." }

    return@withContext ClassLoader.getSystemResourceAsStream(path)?.bufferedReader()
        ?.use(BufferedReader::readText)
        ?.replace(System.lineSeparator(), "\n")
        ?: throw IllegalStateException("Unable to load file [$path]")
}

/**
 * Checks if a file within the classpath exists.
 *
 * **Example**:
 *
 * For _src/main/resources/file.txt_ you can call
 * ```
 * val result = resourceFileExists("file.txt")
 * ```
 * For _src/test/resources/dir/subdir/file.txt_ you can call
 * ```
 * val result = resourceFileExists("dir/subdir/file.txt")
 * ```
 * @since 4.0.0
 * @throws IllegalArgumentException If the given path is blank.
 * @return **true** if the file exists in classpath.
 */
public fun resourceFileExists(path: String, classLoader: ClassLoader = ClassLoader.getSystemClassLoader()): Boolean {
    require(path.neitherNullNorBlank()) { "Given path must not be blank." }

    val resource = classLoader.getResource(path) ?: return false

    return when (resource.protocol) {
        "file" -> Paths.get(resource.toURI()).regularFileExists()
        "jar" -> {
            val jarFile = URI(resource.toString().replace("jar:", EMPTY).substringBefore('!'))
                .toPath()
                .toAbsolutePath()
            val jarEntry = try {
                JarFile(jarFile.toString()).getJarEntry(path)
            } catch (e: ZipException) {
                return false
            }

            return jarEntry != null && !jarEntry.isDirectory
        }
        else -> throw IllegalArgumentException("Unknown protocol.")
    }
}

/**
 * Creates a random number within the interval of two given numbers. It doesn't matter which of the parameters is
 * the min and which one is the max value. Only restriction is that they cannot be equal.
 * @since 1.0.0
 * @param number1 Bound for a random number (inclusive).
 * @param number2 Bound for a random number (inclusive).
 * @throws IllegalStateException If the given numbers are equal.
 * @return A random number withing the given bounds.
 */
public fun random(number1: Int, number2: Int): Long {
    require(number1 != number2) { "Numbers cannot be equal." }

    val min = if (number1 < number2) number1 else number2
    val max = if (number1 > number2) number1 else number2

    return (SecureRandom().nextInt((max - min) + 1) + min).toLong()
}

/**
 * Only executes the given function if the current context is not the test context.
 * @since 14.0.0
 * @param contextAwareImpl Config of a meta data provider.
 * @param func Function to be executed if the current context is not the test context.
 */
public suspend fun excludeFromTestContext(contextAwareImpl: ContextAware, func: suspend () -> Unit): Unit = withContext(LIMITED_CPU) {
    if (!contextAwareImpl.isTestContext()) {
        func.invoke()
    }
}