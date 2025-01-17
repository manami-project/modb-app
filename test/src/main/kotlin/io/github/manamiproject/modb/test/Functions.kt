package io.github.manamiproject.modb.test

import kotlinx.coroutines.*
import java.io.BufferedReader
import java.lang.ClassLoader.getSystemResourceAsStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.readBytes
import kotlin.test.fail

/**
 * Lets you access a file in _src/test/resources_ as [Path].
 *
 * **Example**:
 *
 * For _src/test/resources/file.txt_ you can call
 * ```
 * val file = testResource("file.txt")
 * ```
 * For _src/test/resources/dir/subdir/file.txt_ you can call
 * ```
 * val file = testResource("dir/subdir/file.txt")
 * ```
 *
 * @since 1.0.0
 * @see loadTestResource
 * @return [Path] object of the given file.
 */
public fun testResource(path: String): Path {
    require(path.isNotBlank()) { "Path must not be blank" }
    val resource = ClassLoader.getSystemResource(path)?.toURI() ?: throw IllegalStateException("Path [$path] not found.")

    return Paths.get(resource)
}

/**
 * Reads the content of a file from _src/test/resources_ into a [String].
 * Line separators will always be converted to `\n`
 *
 * **Example**:
 *
 * For _src/test/resources/file.txt_ you can call
 * ```
 * val content = loadTestResource("file.txt")
 * ```
 * For _src/test/resources/dir/subdir/file.txt_ you can call
 * ```
 * val content = loadTestResource("dir/subdir/file.txt")
 * ```
 *
 * @since 1.0.0
 * @see testResource
 * @return Content of a file as [String]
 * @throws IllegalArgumentException If the [path] is blank.
 * @throws IllegalStateException If the given [path] is not a regular file.
 */
public inline fun <reified T> loadTestResource(path: String): T {
    require(path.isNotBlank()) { "Path must not be blank" }

    val file = testResource(path)
    check(Files.exists(file) and Files.isRegularFile(file)) { "[$path] either not exists or is not a file." }

    return when (T::class) {
        String::class -> getSystemResourceAsStream(path)?.bufferedReader()
            ?.use(BufferedReader::readText)
            ?.replace(System.lineSeparator(), "\n") as T
            ?: throw IllegalStateException("Unable to load file [$path]")
        ByteArray::class -> file.readBytes() as T
        else -> throw IllegalStateException("Unsupported file type. String and ByteArray are supported.")
    }
}

/**
 * Lets a test fail with a message that the invocation shouldn't have happened.
 * @since 1.0.0
 * @throws AssertionError
 */
public fun shouldNotBeInvoked(): Nothing = fail("should not be invoked")

/**
 * Allows to test suspend functions which are expected to throw an exception.
 * @since 1.4.0
 * @param func Suspend function to be testet.
 * @return The exception that has been thrown
 * @throws AssertionError In case no exception has been thrown or the exception is it of the expected type.
 */
public inline fun <reified T: Throwable> exceptionExpected(noinline func: suspend CoroutineScope.() -> Unit): Throwable {
    var result: Throwable? = null

    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        result = throwable
    }

    runBlocking {
        CoroutineScope(Job() + CoroutineName("UnitTest") + exceptionHandler).launch {
            func.invoke(this)
        }.join()
    }

    return when (result) {
        null -> fail("No exception has been thrown")
        !is T -> fail("Expected [${T::class.simpleName}] to be thrown, but [${result!!::class.simpleName}] was thrown.")
        else -> result!!
    }
}