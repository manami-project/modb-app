package io.github.manamiproject.modb.core.anime

import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.extensions.normalize
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.UNDEFINED
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.anime.AnimeStatus.UNKNOWN as UNKNOWN_STATUS
import io.github.manamiproject.modb.core.anime.AnimeType.UNKNOWN as UNKNOWN_TYPE
import io.github.manamiproject.modb.core.anime.Duration.Companion.UNKNOWN as UNKNOWN_DURATION
import java.net.URI


/**
 * This class represents an anime directly converted from raw data of a meta data provider.
 * It is also the type used to merge multiple anime of different meta data providers.
 * @since 3.1.0
 * @param _title Main title. Must not be blank.
 * @property title Main title.
 * @param _sources Duplicate-free list of sources from which this anime was created.
 * @property sources Duplicate-free list of sources from which this anime was created.
 * @property type Distribution type. **Default** is [AnimeType.UNKNOWN].
 * @property episodes Number of episodes. **Default** is `0`.
 * @property status Publishing status. **Default** is [AnimeStatus.UNKNOWN].
 * @property animeSeason In which season did the anime premiere.
 * @property picture [URI] to a (large) poster/cover. **Default** is a self created "not found" pic.
 * @property thumbnail [URI] to a thumbnail poster/cover. **Default** is a self created "not found" pic.
 * @property duration Duration of an anime having one episode or average duration of an episode if the anime has more than one episode.
 * @property scores List of scores provided by meta data providers.
 * @param _synonyms Duplicate-free list of alternative titles. Synonyms are case sensitive.
 * @property synonyms Duplicate-free list of alternative titles. Synonyms are case sensitive.
 * @param _relatedAnime Duplicate-free list of links to related anime.
 * @property relatedAnime Duplicate-free list of links to related anime.
 * @param _tags Duplicate-free list of tags. This contains both genres and tags from meta data providers. All tags transformed to lower case.
 * @property tags Duplicate-free list of tags. This contains both genres and tags from meta data providers. All tags are lower case.
 * @property activateChecks Disable any checks upon creating the object. This is only supposed to be used during safe deserialization. If created using `false` you can call [performChecks] manually.
 * @throws IllegalArgumentException if _title is blank or number of episodes is negative.
 */
public data class AnimeRaw(
    private var _title: Title,
    private val _sources: HashSet<URI> = HashSet(),
    val type: AnimeType = UNKNOWN_TYPE,
    val episodes: Episodes = 0,
    val status: AnimeStatus = UNKNOWN_STATUS,
    val animeSeason: AnimeSeason = AnimeSeason(),
    val picture: URI = NO_PICTURE,
    val thumbnail: URI = NO_PICTURE_THUMBNAIL,
    val duration: Duration = UNKNOWN_DURATION,
    private val _synonyms: HashSet<Title> = HashSet(),
    private val _relatedAnime: HashSet<URI> = HashSet(),
    private val _tags: HashSet<Tag> = HashSet(),
    @Transient val activateChecks: Boolean = true,
) {

    private val metaDataProviderScores: HashMap<Hostname, MetaDataProviderScoreValue> = hashMapOf()

    /**
     * Main title.
     * @since 1.0.0
     */
    val title: Title
        get() = _title

    /**
     * Duplicate-free list of sources from which this anime was created.
     * @since 16.6.0
     */
    val sources: HashSet<URI>
        get() = _sources.toHashSet()

    /**
     * Duplicate-free list of alternative titles. Synonyms are case sensitive.
     * @since 16.6.0
     */
    val synonyms: HashSet<Title>
        get() = _synonyms.toHashSet()

    /**
     * Duplicate-free list of links to related anime.
     * @since 16.6.0
     */
    val relatedAnime: HashSet<URI>
        get() = _relatedAnime.toHashSet()

    /**
     * Duplicate-free list of tags. This contains both genres and tags from meta data providers. All tags are lower case.
     * @since 16.6.0
     */
    val tags: HashSet<Tag>
        get() = _tags.toHashSet()

    /**
     * Duplicate-free list of scores.
     * @since 17.0.0
     */
    val scores: HashSet<MetaDataProviderScoreValue>
        get() = metaDataProviderScores.values.toHashSet()

    init {
        require(title.neitherNullNorBlank()) { "Title cannot be blank." }

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
    public fun addSynonyms(vararg synonym: Title): AnimeRaw = addSynonyms(synonym.toHashSet())

    /**
     * Add additional synonyms to the existing list. Duplicates are being ignored.
     * Comparison for this is case sensitive. This will **not** override [synonyms].
     * The value which is present in [title] cannot be added.
     * @since 1.0.0
     * @param synonyms List of synonyms.
     * @return Same instance.
     */
    public fun addSynonyms(synonyms: Collection<Title>): AnimeRaw {
        synonyms.asSequence()
            .map { it.normalize() }
            .filter { it.neitherNullNorBlank() }
            .filter { it != _title }
            .forEach { _synonyms.add(it) }

        return this
    }

    /**
     * Add additional sources to the existing list. This will **not** override [sources].
     * Duplicates are being ignored.
     * @since 3.1.0
     * @param source Sources to be added.
     * @return Same instance.
     */
    public fun addSources(vararg source: URI): AnimeRaw = addSources(source.toHashSet())

    /**
     * Add additional sources to the existing list. This will **not** override [sources].
     * Duplicates are being ignored.
     * @since 3.0.0
     * @param sources List of sources.
     * @return Same instance.
     */
    public fun addSources(sources: Collection<URI>): AnimeRaw {
        _sources.addAll(sources)

        removeRelatedAnimeIf { sources.contains(it) }

        return this
    }

    /**
     * Removes an [URI] from [sources] if the given condition matches.
     * @since 16.6.0
     * @param condition If an entry in [sources] matches this condition, then the [URI] will be removed from [sources].
     * @return Same instance.
     */
    public fun removeSourceIf(condition: (URI) -> Boolean): AnimeRaw {
        _sources.removeIf { condition.invoke(it) }
        return this
    }

    /**
     * Add additional related anime to the existing list. This will **not** override [relatedAnime].
     * Duplicates are being ignored.
     * @since 11.0.0
     * @param relatedAnime List of related anime.
     * @return Same instance.
     */
    public fun addRelatedAnime(vararg relatedAnime: URI): AnimeRaw = addRelatedAnime(relatedAnime.toHashSet())

    /**
     * Add additional related anime to the existing list. This will **not** override [relatedAnime].
     * Duplicates are being ignored.
     * @since 11.0.0
     * @param relatedAnime List of related anime.
     * @return Same instance.
     */
    public fun addRelatedAnime(relatedAnime: Collection<URI>): AnimeRaw {
        relatedAnime.asSequence()
            .filter { !_sources.contains(it) }
            .forEach { _relatedAnime.add(it) }

        return this
    }

    /**
     * Removes an [URI] from [relatedAnime] if the given condition matches.
     * @since 11.0.0
     * @param condition If an entry in [relatedAnime] matches this condition, then the [URI] will be removed from [relatedAnime].
     * @return Same instance.
     */
    public fun removeRelatedAnimeIf(condition: (URI) -> Boolean): AnimeRaw {
        _relatedAnime.removeIf { condition.invoke(it) }
        return this
    }

    /**
     * Add additional tags to the existing list. This will **not** override [tags].
     * Duplicates are being ignored.
     * @since 3.1.0
     * @param tags List of tags.
     * @return Same instance.
     */
    public fun addTags(vararg tags: Tag): AnimeRaw = addTags(tags.toHashSet())

    /**
     * Add additional tags to the existing list. This will **not** override [tags].
     * Duplicates are being ignored.
     * @since 1.0.0
     * @param tags List of tags.
     * @return Same instance.
     */
    public fun addTags(tags: Collection<Tag>): AnimeRaw {
        tags.asSequence()
            .map { it.normalize() }
            .filter { it.neitherNullNorBlank() }
            .map { it.lowercase() }
            .forEach { _tags.add(it) }

        return this
    }

    /**
     * Adds scores provided by meta data providers. Ignores [NoMetaDataProviderScore].
     * Only one instance of a score per meta data provider is supported. Latest score added wins.
     * @since 17.0.0
     * @param scores List of scores provided by meta data providers.
     * @return Same instance.
     */
    public fun addScores(vararg scores: MetaDataProviderScore): AnimeRaw = addScores(scores.toHashSet())

    /**
     * Adds scores provided by meta data providers. Ignores [NoMetaDataProviderScore].
     * Only one instance of a score per meta data provider is supported. Latest score added wins.
     * @since 17.0.0
     * @param scores A score of a meta data provider.
     * @return Same instance.
     */
    public fun addScores(scores: Collection<MetaDataProviderScore>): AnimeRaw {
        scores.filterIsInstance<MetaDataProviderScoreValue>().forEach { score ->
            metaDataProviderScores[score.hostname] = score
        }

        return this
    }

    /**
     * + Title and synonyms of the given [AnimeRaw] will both be added to the [synonyms] of this instance.
     * + All sources of the given [AnimeRaw] will be added to the [sources] of this instance.
     * + All related anime of the given [AnimeRaw] will be added to the [relatedAnime] of this instance.
     * + All tags of the given [AnimeRaw] will be added to the [tags] of this instance.
     * + In case the number of episodes of this instance is 0, the value of the given [AnimeRaw] will be applied.
     * + In case the type of this instance is [AnimeType.UNKNOWN], the value of the given [AnimeRaw] will be applied.
     * + In case the status of this instance is [AnimeStatus.UNKNOWN], the value of the given [AnimeRaw] will be applied.
     * + In case the duration of this instance is [Duration.UNKNOWN], the value of the given [AnimeRaw] will be applied.
     * + In case the season of this instance's [animeSeason] is [UNDEFINED], the season of the given [AnimeRaw] will be applied.
     * + In case the year of this instance's [animeSeason] is [AnimeSeason.UNKNOWN_YEAR], the year if the given [AnimeRaw] will be applied.
     * + All scores are collected. However, only one instance per meta data provider is kept. Latest score added wins.
     * @since 1.0.0
     * @param anime [AnimeRaw] which is being merged into the this instance.
     * @return New instance of the merged anime.
     */
    public fun mergeWith(anime: AnimeRaw): AnimeRaw {
        val mergedEpisodes = if (episodes == 0 && anime.episodes != 0) {
            anime.episodes
        } else {
            episodes
        }

        val mergedType = if (type == UNKNOWN_TYPE && anime.type != UNKNOWN_TYPE) {
            anime.type
        } else {
            type
        }

        val mergedStatus = if (status == UNKNOWN_STATUS && anime.status != UNKNOWN_STATUS) {
            anime.status
        } else {
            status
        }

        val mergedDuration = if (duration == UNKNOWN_DURATION && anime.duration != UNKNOWN_DURATION) {
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

        return AnimeRaw(
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
            .addSynonyms(_synonyms)
            .addSynonyms(anime.title)
            .addSynonyms(anime.synonyms)
            .addRelatedAnime(_relatedAnime)
            .addRelatedAnime(anime.relatedAnime)
            .addTags(_tags)
            .addTags(anime.tags)
            .addScores(scores)
            .addScores(anime.scores)
    }

    /**
     * Performs checks and fixes data.
     * @since 11.0.0
     * @return Same instance of the anime.
     * @throws IllegalArgumentException if _title is blank or number of episodes is negative.
     */
    public fun performChecks(): AnimeRaw {
        _title = _title.normalize()
        require(_title.neitherNullNorBlank()) { "Title cannot be blank." }

        require(episodes >= 0) { "Episodes cannot have a negative value." }

        val uncheckedSources: Collection<URI> = _sources.toSet()
        _sources.clear()
        addSources(uncheckedSources)

        val uncheckedSynonyms: Collection<Title> = _synonyms.toSet()
        _synonyms.clear()
        addSynonyms(uncheckedSynonyms)

        val uncheckedRelatedAnime: Collection<URI> = _relatedAnime.toSet()
        _relatedAnime.clear()
        addRelatedAnime(uncheckedRelatedAnime)

        val uncheckedTags: Collection<Tag> = _tags.toList()
        _tags.clear()
        addTags(uncheckedTags)

        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AnimeRaw

        if (_title != other._title) return false
        if (_sources != other.sources) return false
        if (type != other.type) return false
        if (episodes != other.episodes) return false
        if (status != other.status) return false
        if (animeSeason != other.animeSeason) return false
        if (picture != other.picture) return false
        if (thumbnail != other.thumbnail) return false
        if (duration != other.duration) return false
        if (scores != other.scores) return false
        if (_synonyms != other.synonyms) return false
        if (_relatedAnime != other.relatedAnime) return false
        if (_tags != other.tags) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _title.hashCode()
        result = 31 * result + _sources.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + episodes
        result = 31 * result + status.hashCode()
        result = 31 * result + animeSeason.hashCode()
        result = 31 * result + picture.hashCode()
        result = 31 * result + thumbnail.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + metaDataProviderScores.values.sorted().hashCode()
        result = 31 * result + _synonyms.hashCode()
        result = 31 * result + _relatedAnime.hashCode()
        result = 31 * result + _tags.hashCode()
        return result
    }

    override fun toString(): String {
        return """
            Anime(
              sources = ${_sources.sorted()}
              title = $_title
              type = $type
              episodes = $episodes
              status = $status
              animeSeason = $animeSeason
              picture = $picture
              thumbnail = $thumbnail
              duration = $duration
              scores = ${metaDataProviderScores.values.sortedBy { it.hostname }}
              synonyms = ${_synonyms.sorted()}
              relatedAnime = ${_relatedAnime.sorted()}
              tags = ${_tags.sorted()}
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
}