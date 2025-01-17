package io.github.manamiproject.modb.core.extensions

import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_FS
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.*
import kotlin.io.path.Path as PathCreator

/**
 * A [Path] can represent a directory, file or a link. This typealias is used to specify the usage of a directory.
 * @since 1.0.0
 * @see RegularFile
 */
public typealias Directory = Path

/**
 * A [Path] can represent a directory, file or a link. This typealias is used to specify the usage of a file, link or a symbolic link.
 * @since 1.0.0
 * @see Directory
 */
public typealias RegularFile = Path

/**
 * Creates a new [Path] instance with the file's suffix changed. A file is not created by this. If you want to create it
 * call [createFile] afterwards.
 * + If the file is a regular file in form of `name.suffix` the suffix is simply changed to the given value. `test.yaml` => `test.yml`.
 * + If the file contains multiple suffix parts (**example:** `test.json.BAK`) only the last part is changed. `test.yml.BAK` => `test.yml.backup`.
 * + If the given path is an existing directory, nothing is changed and the given path is returned unchanged. `directory` => `directory`.
 * + If a file does not provide a suffix yet, it will be added. `test` => `test.yml`.
 * + If the path is a hidden file (files whose whole file name starts with a dot **example:** `.gitignore`) the suffix is simply appended. `.gitignore` => `.gitignore.BAK`.
 * @since 1.0.0
 * @param suffix Can be either a suffix with a leading dot (**example:** `.json`) or without a leading dot (**example:** `json`).
 * @return The modified [RegularFile] object.
 */
public fun Path.changeSuffix(suffix: String): RegularFile {
    if (this.directoryExists()) {
        return this
    }

    val directory = this.parent
    val fileName = this.fileName.toString()

    val fileNameWithoutSuffix = if (fileName.startsWith('.')) {
        fileName
    } else {
        fileName.substringBeforeLast('.')
    }

    val newSuffix = if (suffix.startsWith(".")) {
        suffix.substring(1)
    } else {
        suffix
    }

    val fileNameRewrite = if (newSuffix.eitherNullOrBlank()) {
        fileNameWithoutSuffix
    } else {
        "$fileNameWithoutSuffix.$newSuffix"
    }

    return if (directory == null) {
        PathCreator(fileNameRewrite)
    } else {
        directory.resolve(fileNameRewrite)
    }
}

/**
 * Checks whether the given [Path] is a regular file and exists.
 * @since 1.0.0
 * @param linkOption Indicating how symbolic links are handled
 * @returns `false` if one of the checks does not match.
 */
public fun Path.regularFileExists(vararg linkOption: LinkOption): Boolean = this.exists(*linkOption) && Files.isRegularFile(this, *linkOption)

/**
 * Checks whether the given [Path] is a directory and exists.
 * @since 1.0.0
 * @param linkOption Indicating how symbolic links are handled
 * @returns `false` if one of the checks does not match.
 */
public fun Path.directoryExists(vararg linkOption: LinkOption): Boolean = this.exists(*linkOption) && Files.isDirectory(this, *linkOption)

/**
 * Read the content of a file to a [String]
 * @since 8.0.0
 * @param charset The charset to use for decoding. **Default** is [UTF_8]
 * @return The file's content
 * @throws NoSuchFileException if the given [Path] doesn't exist or is not a file.
 * @receiver Any regular file.
 */
public suspend fun RegularFile.readFile(charset: Charset = UTF_8): String = withContext(LIMITED_FS) {
    if (regularFileExists()) {
        readLines(charset).joinToString("\n")
    } else {
        throw NoSuchFileException(this.toString())
    }
}

/**
 * Can copy file to file, directory to directory or a file into a directory.
 * Does not copy the content of directory.
 * @since 8.0.0
 * @param target Target to copy the given [Path] to
 * @param copyOptions Specifying how the copy should be done
 * @return The target as [Path] object
 * @throws FileAlreadyExistsException if the target already exists
 * @receiver Any regular file or directory.
 */
public suspend fun Path.copyTo(target: Path, vararg copyOptions: CopyOption): Path {
    val source = this

    return withContext(IO) {
        val processedTarget = when {
            source.regularFileExists() && target.directoryExists() -> target.resolve(source.fileName)
            source.directoryExists() && target.directoryExists() -> target.resolve(source.fileName)
            else -> target
        }

        return@withContext Files.copy(source, processedTarget, *copyOptions)
    }
}

/**
 * Will return the name of either a [RegularFile] or a [Directory].
 * @since 2.2.0
 * @return The file name as a [String]
 * @receiver Any regular file.
 */
public fun Path.fileName(): String = this.fileName.toString()

/**
 * + Returns the suffix of a file name in form of `{name}.{suffix}`.
 * + Returns the full file name if there is no dot followed by a suffix.
 * + Returns the full name of a hidden file. A hidden file referring to a file name starting with a dot.
 * **Example:** `.gitignore`
 *
 * Behaves the same for a directory.
 * @since 2.2.0
 * @return Either the file suffix or the full file name
 * @receiver Any regular file.
 */
public fun RegularFile.fileSuffix(): FileSuffix {
    val fileName = this.fileName.toString()

    return if (fileName.startsWith('.')) {
        fileName
    } else {
        fileName.substringAfterLast('.')
    }
}

/**
 * Returns a list of the files in this directory optionally filtered by matching against the specified glob pattern.
 * @since 8.0.0
 * @param glob the globbing pattern. The syntax is specified by the [FileSystem.getPathMatcher] method.
 * @return Files matching the [glob] pattern.
 * @receiver A directory
 * @throws java.util.regex.PatternSyntaxException if the glob pattern is invalid.
 * @throws NotDirectoryException If this path does not refer to a directory.
 * @throws java.io.IOException - If an I/O error occurs.
 */
public suspend fun Directory.listRegularFiles(glob: String = "*"): Collection<RegularFile> {
    val dir = this

    return withContext(LIMITED_FS) {
        dir.listDirectoryEntries(glob = glob)
            .filter { it.regularFileExists() }
    }
}

/**
 * Packs multiple regular files into a single zip file.
 * @since 14.0.0
 * @param files Files to be included in the zip files.
 * @receiver Any regular file which will be the zip file.
 * @return Returns the instance of the zip file which is also the receiver.
 */
public suspend fun RegularFile.createZipOf(vararg files: RegularFile): RegularFile {
    require(this.isRegularFile() || !this.exists()) { "Receiver must be a regular file." }
    require(files.all { it.regularFileExists() }) { "Can only include regular files which exist." }

    val target = this

    withContext(LIMITED_FS) {
        target.outputStream().use { fileOutputStream ->
            ZipOutputStream(fileOutputStream).use { zipOutputStream ->
                files.forEach { currentFile ->
                    zipOutputStream.putNextEntry(ZipEntry(currentFile.fileName.toString()))
                    currentFile.inputStream().use { fileInputStream ->
                        fileInputStream.copyTo(zipOutputStream)
                    }
                }
            }
        }
    }

    return target
}