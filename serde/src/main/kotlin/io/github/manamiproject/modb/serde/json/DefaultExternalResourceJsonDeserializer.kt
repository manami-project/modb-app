package io.github.manamiproject.modb.serde.json

import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_FS
import io.github.manamiproject.modb.core.extensions.*
import io.github.manamiproject.modb.core.httpclient.DefaultHttpClient
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream


/**
 * # What it does
 * + Can download JSON files from [manami-project/anime-offline-database](https://github.com/manami-project/anime-offline-database) via HTTPS and deserialize them.
 * + Can download JSON files provided as zip file from [manami-project/anime-offline-database](https://github.com/manami-project/anime-offline-database) via HTTPS and deserialize them.
 * + Can deserialize the files from [manami-project/anime-offline-database](https://github.com/manami-project/anime-offline-database) as local JSON file.
 * + Can deserialize the files from [manami-project/anime-offline-database](https://github.com/manami-project/anime-offline-database) as local JSON file if it is provided as zip file.
 *
 * # Usage
 * Wrap an instance of [JsonDeserializer] in a [DefaultExternalResourceJsonDeserializer] to be able to deserialize a [URL] or a [RegularFile].
 * ```kotlin
 * val animeListDeserializer = DefaultExternalResourceJsonDeserializer<List<Anime>>(deserializer = AnimeListJsonStringDeserializer())
 * val deadEntriesDeserializer = DefaultExternalResourceJsonDeserializer<List<AnimeId>>(deserializer = DeadEntriesJsonStringDeserializer())
 * ```
 * Now you can either deserialize the anime dataset file or a dead entries file by using a [URL] or a [RegularFile].
 * To deserialize a JSON [String] use [AnimeListJsonStringDeserializer] or [DeadEntriesJsonStringDeserializer].
 * The [DefaultExternalResourceJsonDeserializer] can also handle zipped files, but the zip file must only contain a single JSON file.
 * **Example:**
 * ```kotlin
 * val deserializer = DefaultExternalResourceJsonDeserializer<List<Anime>>(deserializer = AnimeListJsonStringDeserializer())
 * val allAnime: List<Dataset> = deserializer.deserialize(URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/anime-offline-database-minified.json").toURL())
 * ```
 * @since 5.0.0
 * @param httpClient Used to download given [URL]s
 * @param deserializer Deserializer for either the dataset or dead entries file.
 */
public class DefaultExternalResourceJsonDeserializer<out T>(
    private val httpClient: HttpClient = DefaultHttpClient.instance,
    private val deserializer: JsonDeserializer<T>,
) : ExternalResourceJsonDeserializer<T> {

    override suspend fun deserialize(url: URL): T = withContext(LIMITED_CPU) {
        log.info { "Downloading dataset file from [$url]" }

        val response = httpClient.get(url)

        when {
            !response.isOk() -> throw IllegalStateException("Error downloading file: HTTP response code was: [${response.code}]")
            response.body.isEmpty() || response.bodyAsText.eitherNullOrBlank() -> throw IllegalStateException("Error downloading file: The response body was blank.")
        }

        return@withContext when {
            response.headers["content-type"]?.contains("application/zip") == true -> deserializer.deserialize(readZip(response.body))
            else -> deserializer.deserialize(response.bodyAsText)
        }
    }

    override suspend fun deserialize(file: RegularFile): T = withContext(LIMITED_FS) {
        require(file.regularFileExists()) { "The given path does not exist or is not a regular file: [${file.toAbsolutePath()}]" }

        val content =  when(file.fileSuffix()) {
            "json" -> file.readFile()
            "zip" -> readZipFile(file)
            else -> throw IllegalArgumentException("File is neither JSON nor zip file")
        }

        log.info { "Reading dataset file." }

        return@withContext deserializer.deserialize(content)
    }

    private suspend fun readZipFile(file: RegularFile): String = withContext(LIMITED_FS) {
        return@withContext ZipFile(file.toAbsolutePath().toString()).use { zip ->
            val zipEntries = zip.entries().toList()
            require(zipEntries.size == 1) { "The zip file contains more than one file." }

            val entry = zipEntries.first()
            require(entry.name.endsWith("json")) { "File inside zip archive is not a JSON file." }

            zip.getInputStream(entry).bufferedReader().use { reader ->
                reader.readText()
            }
        }
    }

    private suspend fun readZip(bytes: ByteArray): String = withContext(LIMITED_CPU) {
        var ret = EMPTY
        ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
            val entry: ZipEntry = zis.nextEntry!!

            if (entry.isDirectory) {
                throw IllegalStateException("Zip file contains directory: [${entry.name}]")
            }

            if (!entry.name.endsWith(".json")) {
                throw IllegalStateException("File inside downloaded zip file is not a JSON file: [${entry.name}]")
            }

            ret = zis.bufferedReader().readText()

            if (zis.nextEntry != null) {
                zis.closeEntry()
                throw IllegalStateException("Zip file contains more than two files.")
            }

            zis.closeEntry()
        }
        return@withContext ret
    }

    private companion object {
        private val log by LoggerDelegate()
    }
}