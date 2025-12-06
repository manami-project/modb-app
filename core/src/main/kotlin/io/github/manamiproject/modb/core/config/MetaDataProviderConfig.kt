package io.github.manamiproject.modb.core.config

import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.remove
import java.net.URI

/**
 * @since 1.0.0
 */
public typealias Hostname = String

/**
 * @since 1.0.0
 */
public typealias FileSuffix = String

/**
 * @since 1.0.0
 */
public typealias AnimeId = String

/**
 * Configuration for a specific meta data provider. A meta data provider is a website providing data about anime such as `myanimelist.net` or `kitsu.app`.
 * @since 1.0.0
 */
public interface MetaDataProviderConfig: ContextAware {

    /**
     * Hostname of the meta data provider. **Example:** `myanimelist.net`.
     * @since 1.0.0
     * @return Hostname without protocol or leading `www`.
     */
    public fun hostname(): Hostname

    /**
     * Builds the [URI] which is a direct link to an anime on the mata data provider's website.
     * @since 3.0.0
     * @param id Id of the anime on the meta data provider's website.
     * @return Direct link to the anime on the website of the meta data provider.
     * @see extractAnimeId
     */
    public fun buildAnimeLink(id: AnimeId): URI = URI("https://${hostname()}/anime/$id")

    /**
     * Builds the [URI] which is used to download data.
     * This can be the same as the [URI] created in [buildAnimeLink] or in case of GraphQL, for example, a totally different [URI].
     * @since 3.0.0
     * @param id Identifier within the [URI]. **Default** is an empty [String]
     * @return [URI] from which anime data can be downloaded.
     */
    public fun buildDataDownloadLink(id: String = EMPTY): URI = buildAnimeLink(id)

    /**
     * Extracts the [AnimeId] from a given [URI].
     * @since 3.0.0
     * @param uri Anime link URI. Could've been previously created using [buildAnimeLink].
     * @return The ID for the anime.
     * @throws IllegalArgumentException if the given [URI] does not contain the hostname.
     * @see buildAnimeLink
     */
    public fun extractAnimeId(uri: URI): AnimeId {
        require(uri.toString().contains(hostname())) { "URI doesn't contain hostname [${hostname()}]" }
        return uri.toString().remove(buildAnimeLink(EMPTY).toString())
    }

    /**
     * File suffix for the file in which the raw data is saved. No leading dot. **Example:** `html`.
     * @since 1.0.0
     * @return File suffix without a leading dot.
     */
    public fun fileSuffix(): FileSuffix
}