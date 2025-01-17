package io.github.manamiproject.modb.test

import kotlinx.coroutines.runBlocking
import java.io.Closeable
import java.nio.file.Files
import java.nio.file.Path

/**
 * Helper class for [tempDirectory]
 * @since 1.0.0
 * @see tempDirectory
 * @param prefix Prefix of the temporary directory.
 */
public class TempDirectory(prefix: String = "modb_") : Closeable {

    public val tempDir: Path = Files.createTempDirectory(prefix)

    override fun close() {
        tempDir.toFile().deleteRecursively()
    }
}

/**
 * Creates a temporary directory and ensures that it's removed after test execution even if an exception occurred.
 * The temporary directory is prefixed with _modb__.
 * You can access the temporary directory within [action] using the variable `tempDir`.
 *
 * **Example**
 *
 * ```
 * tempDirectory {
 *     println(Files.exist(tempDir))
 * }
 * ```
 *
 * @since 1.4.0
 * @param action Can take any action including suspend functions.
 */
public fun tempDirectory(action: suspend TempDirectory.() -> Unit) {
    val tempDir = TempDirectory()

    tempDir.use {
        runBlocking {
            it.action()
        }
    }
}