package io.github.manamiproject.modb.core.models

import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.extensions.normalize
import io.github.manamiproject.modb.core.models.AnimeSeason.Season.UNDEFINED
import java.net.URI

/**
 * @since 1.0.0
 */
public typealias Episodes = Int

/**
 * @since 3.0.0
 */
public typealias Tag = String

/**
 * @since 3.0.0
 */
public typealias Title = String


/**
 * @since 3.1.0
 * @property _title Main title. Must not be blank.
 * @property sources Duplicate-free list of sources from which this anime was created.
 * @property synonyms Duplicate-free list of alternative titles. Synonyms are case sensitive.
 * @property type Distribution type. **Default** is [Anime.Type.UNKNOWN].
 * @property episodes Number of episodes. **Default** is `0`.
 * @property status Publishing status. **Default** is [Anime.Status.UNKNOWN].
 * @property animeSeason In which season did the anime premiere.
 * @property picture [URI] to a (large) poster/cover. **Default** is a self created "not found" pic.
 * @property thumbnail [URI] to a thumbnail poster/cover. **Default** is a self created "not found" pic.
 * @property duration Duration of an anime having one episode or average duration of an episode if the anime has more than one episode.
 * @property relatedAnime Duplicate-free list of related anime.
 * @property tags Duplicate-free list of tags. All tags are lower case.
 * @property activateChecks Disable any checks upon creating the object. This is only supposed to be used during safe deserialization. If created using `false` you can call [performChecks] manually.
 * @throws IllegalArgumentException if _title is blank or number of episodes is negative.
 */
public data class Anime(
    private var _title: Title,
    val sources: HashSet<URI> = HashSet(),
    val type: Type = Type.UNKNOWN,
    val episodes: Episodes = 0,
    val status: Status = Status.UNKNOWN,
    val animeSeason: AnimeSeason = AnimeSeason(),
    val picture: URI = NO_PICTURE,
    val thumbnail: URI = NO_PICTURE_THUMBNAIL,
    val duration: Duration = Duration.UNKNOWN,
    val synonyms: HashSet<Title> = HashSet(),
    val relatedAnime: HashSet<URI> = HashSet(),
    val tags: HashSet<Tag> = HashSet(),
    @Transient val activateChecks: Boolean = true,
) {

    /**
     * Main title.
     * @since 1.0.0
     */
    val title: Title
        get() = _title

    init {
        if (activateChecks) {
            performChecks()
        }
    }

    /**
     * Add additional synonyms to the existing list. Duplicates are being ignored.
     * Comparison for this is case sensitive. This will **not** override [synonyms].
     * The value which is present in [title] cannot be added.
     * @since 3.1.0
     * @param synonym Synonyms to be added.
     * @return Same instance.
     */
    public fun addSynonyms(vararg synonym: Title): Anime = addSynonyms(synonym.toHashSet())

    /**
     * Add additional synonyms to the existing list. Duplicates are being ignored.
     * Comparison for this is case sensitive. This will **not** override [synonyms].
     * The value which is present in [title] cannot be added.
     * @since 1.0.0
     * @param synonyms List of synonyms.
     * @return Same instance.
     */
    public fun addSynonyms(synonyms: Collection<Title>): Anime {
        synonyms.asSequence()
            .map { it.normalize() }
            .filter { it.neitherNullNorBlank() }
            .filter { it != _title }
            .forEach { this.synonyms.add(it) }

        return this
    }

    /**
     * Add additional sources to the existing list. This will **not** override [sources].
     * Duplicates are being ignored.
     * @since 3.1.0
     * @param source Sources to be added.
     * @return Same instance.
     */
    public fun addSources(vararg source: URI): Anime = addSources(source.toHashSet())

    /**
     * Add additional sources to the existing list. This will **not** override [sources].
     * Duplicates are being ignored.
     * @since 3.0.0
     * @param sources List of sources.
     * @return Same instance.
     */
    public fun addSources(sources: Collection<URI>): Anime {
        this.sources.addAll(sources)

        removeRelatedAnimeIf { sources.contains(it) }

        return this
    }

    /**
     * Removes an [URI] from [sources] if the given condition matches.
     * @since 16.6.0
     * @param condition If an entry in [sources] matches this condition, then the [URI] will be removed from [sources].
     * @return Same instance.
     */
    public fun removeSourceIf(condition: (URI) -> Boolean): Anime {
        sources.removeIf { condition.invoke(it) }
        return this
    }

    /**
     * Add additional related anime to the existing list. This will **not** override [relatedAnime].
     * Duplicates are being ignored.
     * @since 11.0.0
     * @param relatedAnime List of related anime.
     * @return Same instance.
     */
    public fun addRelatedAnime(vararg relatedAnime: URI): Anime = addRelatedAnime(relatedAnime.toHashSet())

    /**
     * Add additional related anime to the existing list. This will **not** override [relatedAnime].
     * Duplicates are being ignored.
     * @since 11.0.0
     * @param relatedAnime List of related anime.
     * @return Same instance.
     */
    public fun addRelatedAnime(relatedAnime: Collection<URI>): Anime {
        relatedAnime.asSequence()
            .filter { !sources.contains(it) }
            .forEach { this.relatedAnime.add(it) }

        return this
    }

    /**
     * Removes an [URI] from [relatedAnime] if the given condition matches.
     * @since 11.0.0
     * @param condition If an entry in [relatedAnime] matches this condition, then the [URI] will be removed from [relatedAnime].
     * @return Same instance.
     */
    public fun removeRelatedAnimeIf(condition: (URI) -> Boolean): Anime {
        relatedAnime.removeIf { condition.invoke(it) }
        return this
    }

    /**
     * Add additional tags to the existing list. This will **not** override [tags].
     * Duplicates are being ignored.
     * @since 3.1.0
     * @param tag List of tags.
     * @return Same instance.
     */
    public fun addTags(vararg tag: Tag): Anime = addTags(tag.toHashSet())

    /**
     * Add additional tags to the existing list. This will **not** override [tags].
     * Duplicates are being ignored.
     * @since 1.0.0
     * @param tags List of tags.
     * @return Same instance.
     */
    public fun addTags(tags: Collection<Tag>): Anime {
        tags.asSequence()
            .map { it.normalize() }
            .filter { it.neitherNullNorBlank() }
            .map { it.lowercase() }
            .forEach { this.tags.add(it) }

        return this
    }

    /**
     * + Title and synonyms of the given [Anime] will both be added to the [synonyms] of this instance.
     * + All sources of the given [Anime] will be added to the [sources] of this instance.
     * + All related anime of the given [Anime] will be added to the [relatedAnime] of this instance.
     * + All tags of the given [Anime] will be added to the [tags] of this instance.
     * + In case the number of episodes of this instance is 0, the value of the given [Anime] will be applied.
     * + In case the type of this instance is [Type.UNKNOWN], the value of the given [Anime] will be applied.
     * + In case the status of this instance is [Status.UNKNOWN], the value of the given [Anime] will be applied.
     * + In case the duration of this instance is [Duration.UNKNOWN], the value of the given [Anime] will be applied.
     * + In case the season of this instance's [animeSeason] is [UNDEFINED], the season of the given [Anime] will be applied.
     * + In case the year of this instance's [animeSeason] is [AnimeSeason.UNKNOWN_YEAR], the year if the given [Anime] will be applied.
     * @since 1.0.0
     * @param anime [Anime] which is being merged into the this instance.
     * @return New instance of the merged anime.
     */
    public fun mergeWith(anime: Anime): Anime {
        val mergedEpisodes = if (episodes == 0 && anime.episodes != 0) {
            anime.episodes
        } else {
            episodes
        }

        val mergedType = if (type == Type.UNKNOWN && anime.type != Type.UNKNOWN) {
            anime.type
        } else {
            type
        }

        val mergedStatus = if (status == Status.UNKNOWN && anime.status != Status.UNKNOWN) {
            anime.status
        } else {
            status
        }

        val mergedDuration = if (duration == Duration.UNKNOWN && anime.duration != Duration.UNKNOWN) {
            anime.duration
        } else {
            duration
        }

        val mergedSeason = if (animeSeason.season == UNDEFINED && anime.animeSeason.season != UNDEFINED) {
            anime.animeSeason.season
        } else {
            animeSeason.season
        }

        val mergedYear = if (animeSeason.isYearOfPremiereUnknown() && anime.animeSeason.isYearOfPremiereKnown()) {
            anime.animeSeason.year
        } else {
            animeSeason.year
        }

        return Anime(
            _title = title,
            type = mergedType,
            episodes = mergedEpisodes,
            status = mergedStatus,
            picture = picture,
            thumbnail = thumbnail,
            duration = mergedDuration,
            animeSeason = AnimeSeason(
                season = mergedSeason,
                year = mergedYear,
            ),
        ).addSources(sources)
            .addSources(anime.sources)
            .addSynonyms(synonyms)
            .addSynonyms(anime.title)
            .addSynonyms(anime.synonyms)
            .addRelatedAnime(relatedAnime)
            .addRelatedAnime(anime.relatedAnime)
            .addTags(tags)
            .addTags(anime.tags)
    }

    /**
     * Performs checks and fixes data.
     * @since 11.0.0
     * @return Same instance of the anime.
     * @throws IllegalArgumentException if _title is blank or number of episodes is negative.
     */
    public fun performChecks(): Anime {
        _title = _title.normalize()
        require(_title.neitherNullNorBlank()) { "Title cannot be blank." }

        require(episodes >= 0) { "Episodes cannot have a negative value." }

        val uncheckedSources: Collection<URI> = sources.toSet()
        sources.clear()
        addSources(uncheckedSources)

        val uncheckedSynonyms: Collection<Title> = synonyms.toSet()
        synonyms.clear()
        addSynonyms(uncheckedSynonyms)

        val uncheckedRelatedAnime: Collection<URI> = relatedAnime.toSet()
        relatedAnime.clear()
        addRelatedAnime(uncheckedRelatedAnime)

        val uncheckedTags: Collection<Tag> = tags.toList()
        tags.clear()
        addTags(uncheckedTags)

        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Anime

        if (_title != other._title) return false
        if (sources != other.sources) return false
        if (synonyms != other.synonyms) return false
        if (type != other.type) return false
        if (episodes != other.episodes) return false
        if (status != other.status) return false
        if (animeSeason != other.animeSeason) return false
        if (picture != other.picture) return false
        if (thumbnail != other.thumbnail) return false
        if (duration != other.duration) return false
        if (relatedAnime != other.relatedAnime) return false
        if (tags != other.tags) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _title.hashCode()
        result = 31 * result + sources.hashCode()
        result = 31 * result + synonyms.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + episodes
        result = 31 * result + status.hashCode()
        result = 31 * result + animeSeason.hashCode()
        result = 31 * result + picture.hashCode()
        result = 31 * result + thumbnail.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + relatedAnime.hashCode()
        result = 31 * result + tags.hashCode()
        return result
    }

    override fun toString(): String {
        return """
            Anime(
              sources = ${sources.sorted()}
              title = $_title
              synonyms = ${synonyms.sorted()}
              type = $type
              episodes = $episodes
              status = $status
              animeSeason = $animeSeason
              picture = $picture
              thumbnail = $thumbnail
              duration = $duration
              relatedAnime = ${relatedAnime.sorted()}
              tags = ${tags.sorted()}
            )
        """.trimIndent()
    }

    public companion object {
        /**
         * URL to a default picture.
         * @since 11.0.0
         */
        public val NO_PICTURE: URI = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png")

        /**
         * URL to a default thumbnail.
         * @since 11.0.0
         */
        public val NO_PICTURE_THUMBNAIL: URI = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png")
    }

    /**
     * Distribution type of an anime.
     * @since 1.0.0
     */
    public enum class Type {
        /**
         * @since 1.0.0
         */
        TV,
        /**
         * @since 1.0.0
         */
        MOVIE,
        /**
         * Original Video Animation. See [Wikipedia](https://en.wikipedia.org/wiki/Original_video_animation).
         * @since 1.0.0
         */
        OVA,
        /**
         * Original Net Animation. See [Wikipedia](https://en.wikipedia.org/wiki/Original_net_animation).
         * @since 1.0.0
         */
        ONA,
        /**
         * Basically anything else. Could be music videos, advertisements, manner movies or actual speical episodes.
         * @since 1.0.0
         */
        SPECIAL,
        /**
         * Type is unknown.
         * @since 5.0.0
         */
        UNKNOWN;

        public companion object {
            /**
             * Creates [Anime.Type] from a [String]. Tolerant by ignoreing leading and trailing whitespaces as well as case.
             * @since 11.0.0
             * @param value The value being mapped to a [Anime.Type].
             */
            public fun of(value: String): Type {
                return Type.entries.find { it.toString().equals(value.trim(), ignoreCase = true) } ?: UNKNOWN
            }
        }
    }

    /**
     * Distribution status of an anime.
     * @since 1.0.0
     */
    public enum class Status {
        /**
         * Finished airing or has been released completely.
         * @since 1.0.0
         */
        FINISHED,
        /**
         * Currently airing or releasing.
         * @since 5.0.0
         */
        ONGOING,
        /**
         * Not yet released or aired.
         * @since 1.0.0
         */
        UPCOMING,
        /**
         * Status is unknown.
         * @since 1.0.0
         */
        UNKNOWN;

        public companion object {
            /**
             * Creates [Anime.Status] from a [String]. Tolerant by ignoreing leading and trailing whitespaces as well as case.
             * @since 11.0.0
             * @param value The value being mapped to a [Anime.Status]
             */
            public fun of(value: String): Status {
                return Status.entries.find { it.toString().equals(value.trim(), ignoreCase = true) } ?: UNKNOWN
            }
        }
    }
}