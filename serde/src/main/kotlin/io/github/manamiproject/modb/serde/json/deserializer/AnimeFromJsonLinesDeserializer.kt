package io.github.manamiproject.modb.serde.json.deserializer

import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.io.LifecycleAwareInputStream
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map

/**
 * Deserialzes JSON lines file of [Anime] provided by a [LifecycleAwareInputStream].
 * [manami-project/anime-offline-database](https://github.com/manami-project/anime-offline-database).
 * @since 6.0.0
 * @throws IllegalArgumentException if the given [LifecycleAwareInputStream] is closed.
 */
public class AnimeFromJsonLinesDeserializer: Deserializer<LifecycleAwareInputStream, Flow<Anime>> {

    override suspend fun deserialize(source: LifecycleAwareInputStream): Flow<Anime> {
        require(source.isNotClosed()) { "Stream must not be closed." }

        log.info { "Deserializing JSON lines." }

        return source.bufferedReader()
            .lineSequence()
            .drop(1)
            .asFlow()
            .map { Json.parseJson<Anime>(it)!! }
    }

    public companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [AnimeFromJsonLinesDeserializer]
         * @since 6.0.0
         */
        public val instance: AnimeFromJsonLinesDeserializer by lazy { AnimeFromJsonLinesDeserializer() }
    }
}