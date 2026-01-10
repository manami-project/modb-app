package io.github.manamiproject.modb.serde.json.deserializer

import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.io.LifecycleAwareInputStream
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.serde.json.models.Dataset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

/**
 * Deserializes default JSON of [Anime] provided by a [LifecycleAwareInputStream].
 * This class cannot handle JSON lines. This class can only process default JSON.
 * @since 6.0.0
 * @throws IllegalArgumentException if the given [LifecycleAwareInputStream] is closed.
 */
public class AnimeFromJsonInputStreamDeserializer: Deserializer<LifecycleAwareInputStream, Flow<Anime>> {

    override suspend fun deserialize(source: LifecycleAwareInputStream): Flow<Anime> {
        require(source.isNotClosed()) { "Stream must not be closed." }

        log.info { "Deserializing JSON lines." }

        return source.use { Json.parseJson<Dataset>(source)!!.data.asFlow() }
    }

    public companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [AnimeFromJsonInputStreamDeserializer]
         * @since 6.0.0
         */
        public val instance: AnimeFromJsonInputStreamDeserializer by lazy { AnimeFromJsonInputStreamDeserializer() }
    }
}