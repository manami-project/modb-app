package io.github.manamiproject.modb.core.anime

import io.github.manamiproject.modb.core.anime.AnimeMedia.NO_PICTURE
import io.github.manamiproject.modb.core.anime.AnimeMedia.NO_PICTURE_THUMBNAIL
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import java.net.URI
import io.github.manamiproject.modb.core.anime.AnimeStatus.UNKNOWN as UNKNOWN_STATUS
import io.github.manamiproject.modb.core.anime.AnimeType.UNKNOWN as UNKNOWN_TYPE
import io.github.manamiproject.modb.core.anime.Duration.Companion.UNKNOWN as UNKNOWN_DURATION

/**
 * This class represents the finalized and immutable anime which can be found in the dataset.
 * @since 17.0.0
 * @property title Main title. Must not be blank.
 * @property sources Duplicate-free list of sources from which this anime was created.
 * @property type Distribution type. **Default** is [AnimeType.UNKNOWN].
 * @property episodes Number of episodes. **Default** is `0`.
 * @property status Publishing status. **Default** is [AnimeStatus.UNKNOWN].
 * @property animeSeason In which season did the anime premiere.
 * @property picture [URI] to a (large) poster/cover. **Default** is a self created "not found" pic.
 * @property thumbnail [URI] to a thumbnail poster/cover. **Default** is a self created "not found" pic.
 * @property duration Duration of an anime having one episode or average duration of an episode if the anime has more than one episode.
 * @property score Score based on the different scores of the metadata providers.
 * @property synonyms Duplicate-free list of alternative titles. Synonyms are case-sensitive.
 * @property studios Lower case studio names. In general a duplicate free list, but might contain duplicates for different writings.
 * @property producers Lower case producers names. Companies only. In general a duplicate free list, but might contain duplicates for different writings.
 * @property relatedAnime Duplicate-free list of links to related anime.
 * @property tags Duplicate-free list of tags. This contains both genres and tags from metadata providers. All tags are lower case.
 */
public data class Anime(
    var title: Title,
    val sources: HashSet<URI> = HashSet(),
    val type: AnimeType = UNKNOWN_TYPE,
    val episodes: Episodes = 0,
    val status: AnimeStatus = UNKNOWN_STATUS,
    val animeSeason: AnimeSeason = AnimeSeason(),
    val picture: URI = NO_PICTURE,
    val thumbnail: URI = NO_PICTURE_THUMBNAIL,
    val duration: Duration = UNKNOWN_DURATION,
    val score: Score = NoScore,
    val synonyms: HashSet<Title> = HashSet(),
    val studios: HashSet<Studio> = HashSet(),
    val producers: HashSet<Producer> = HashSet(),
    val relatedAnime: HashSet<URI> = HashSet(),
    val tags: HashSet<Tag> = HashSet(),
) {

    init {
        require(title.neitherNullNorBlank()) { "Title cannot be blank." }
    }

    override fun toString(): String {
        return """
            Anime(
              sources      = ${sources.sorted()}
              title        = $title
              type         = $type
              episodes     = $episodes
              status       = $status
              animeSeason  = $animeSeason
              picture      = $picture
              thumbnail    = $thumbnail
              duration     = $duration
              score        = $score
              synonyms     = ${synonyms.sorted()}
              studios      = ${studios.sorted()}
              producers    = ${producers.sorted()}
              relatedAnime = ${relatedAnime.sorted()}
              tags         = ${tags.sorted()}
            )
        """.trimIndent()
    }
}
