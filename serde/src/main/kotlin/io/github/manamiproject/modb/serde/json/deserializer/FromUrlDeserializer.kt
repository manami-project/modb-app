package io.github.manamiproject.modb.serde.json.deserializer

import com.github.luben.zstd.ZstdInputStream
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_NETWORK
import io.github.manamiproject.modb.core.httpclient.DefaultHttpClient
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.io.LifecycleAwareInputStream
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlinx.coroutines.withContext
import java.net.URL

/**
 * Deserializes content retrieved from an [URL] to any type [T].
 * @since 6.0.0
 */
public class FromUrlDeserializer<out T>(
    private val httpClient: HttpClient = DefaultHttpClient.instance,
    private val deserializer: Deserializer<LifecycleAwareInputStream, T>,
): Deserializer<URL, T> {

    override suspend fun deserialize(source: URL): T = withContext(LIMITED_NETWORK) {
        log.info { "Downloading dataset file from [$source]" }

        val response = httpClient.get(source)

        check(response.isOk()) { "Error downloading file: HTTP response code was: [${response.code}]" }

        val inputStream = when (response.headers["content-type"]?.joinToString()) {
            "application/json" -> LifecycleAwareInputStream(response.bodyAsStream())
            "application/zstd" -> LifecycleAwareInputStream(ZstdInputStream(response.bodyAsStream()))
            else -> throw IllegalStateException("Unsupported content-type: ${response.headers["content-type"]}")
        }

        return@withContext deserializer.deserialize(inputStream)
    }

    public companion object {
        private val log by LoggerDelegate()
    }
}