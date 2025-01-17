package io.github.manamiproject.modb.core.extensions

import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_FS
import kotlinx.coroutines.withContext
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeBytes

/**
 * Writes a [ByteArray] into a [RegularFile]. If the file already exists it will be overwritten.
 * @since 10.1.0
 * @param file The file to which you want to write the given [ByteArray].
 * @param writeLockFile You can choose to write an empty lock file which indicates that the file is currently being created.
 * First the empty lock file is created using [LOCK_FILE_SUFFIX]. Then the actual file is being written. After that the lock file is deleted again.
 * **Default** is `false`.
 * @throws IllegalStateException if the given [ByteArray] is empty.
 * @receiver Any non-nullable [ByteArray].
 */
public suspend fun ByteArray.writeToFile(file: RegularFile, writeLockFile: Boolean = false) {
    val content = this

    withContext(LIMITED_FS) {
        check(content.isNotEmpty()) { "Tried to write file [$file], but the ByteArray was empty." }

        val lockFile = file.changeSuffix(LOCK_FILE_SUFFIX)

        if (writeLockFile) {
            lockFile.createFile()
        }

        file.writeBytes(content)

        if (lockFile.regularFileExists() && writeLockFile) {
            lockFile.deleteIfExists()
        }
    }
}