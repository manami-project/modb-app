package io.github.manamiproject.modb.serde.json.deserializer

import com.github.luben.zstd.ZstdInputStream
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_FS
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.extensions.fileSuffix
import io.github.manamiproject.modb.core.extensions.regularFileExists
import io.github.manamiproject.modb.core.io.LifecycleAwareInputStream
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlinx.coroutines.withContext
import kotlin.io.path.inputStream

/**
 * Deserializes content retrieved from a [RegularFile] to a any type [T].
 * This class handles the files and delegates the actual deserialization to another instance.
 * @since 6.0.0
 * @property deserializer The deserializer used to actually deserialze the data.
 */
public class FromRegularFileDeserializer<out T>(
    private val deserializer: Deserializer<LifecycleAwareInputStream, T>,
): Deserializer<RegularFile, T> {

    override suspend fun deserialize(source: RegularFile): T = withContext(LIMITED_FS) {
        require(source.regularFileExists()) { "The given path does not exist or is not a regular file: [${source.toAbsolutePath()}]" }

        log.info { "Reading dataset file." }

        val inputStream = when (source.fileSuffix()) {
            "json" -> LifecycleAwareInputStream(source.inputStream())
            "zst" -> LifecycleAwareInputStream(ZstdInputStream(source.inputStream()))
            else -> throw IllegalArgumentException("File of type [${source.fileSuffix()}] is not supported.]")
        }

        return@withContext deserializer.deserialize(inputStream)
    }

    public companion object {
        private val log by LoggerDelegate()
    }
}