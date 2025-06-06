package io.github.manamiproject.modb.core.json

import com.squareup.moshi.*
import io.github.manamiproject.modb.core.anime.*
import io.github.manamiproject.modb.core.anime.AnimeMedia.NO_PICTURE
import io.github.manamiproject.modb.core.anime.AnimeMedia.NO_PICTURE_THUMBNAIL
import io.github.manamiproject.modb.core.extensions.EMPTY
import java.net.URI
import io.github.manamiproject.modb.core.anime.AnimeStatus.UNKNOWN as UNKNOWN_STATUS
import io.github.manamiproject.modb.core.anime.AnimeType.UNKNOWN as UNKNOWN_TYPE
import io.github.manamiproject.modb.core.anime.Duration.Companion.UNKNOWN as UNKNOWN_DURATION

internal class AnimeRawAdapter: JsonAdapter<AnimeRaw>() {

    private val titleAdapter = TitleAdapter()
    private val titleHashSetAdapter = HashSetAdapter(TitleAdapter())
    private val uriAdapter = UriAdapter()
    private val typeAdapter = AnimeTypeAdapter()
    private val statusAdapter = AnimeStatusAdapter()
    private val animeSeasonAdapter = AnimeSeasonAdapter()
    private val durationAdapter = DurationAdapter()
    private val metaDataProviderScoreValueAdapter = HashSetAdapter(MetaDataProviderScoreValueAdapter())
    private val studioHashSetAdapter = HashSetAdapter(StudioAdapter())
    private val producerHashSetAdapter = HashSetAdapter(ProducerAdapter())
    private val uriHashSetAdapter = HashSetAdapter(UriAdapter())
    private val tagHashSetAdapter = HashSetAdapter(TagAdapter())

    @FromJson
    override fun fromJson(reader: JsonReader): AnimeRaw {
        reader.beginObject()

        var title = EMPTY
        var titleDeserialized = false
        var sources = HashSet<URI>()
        var sourcesDeserialized = false
        var type = UNKNOWN_TYPE
        var typeDeserialized = false
        var episodes = 0
        var episodesDeserialized = false
        var status = UNKNOWN_STATUS
        var statusDeserialized = false
        var animeSeason = AnimeSeason()
        var animeSeasonDeserialized = false
        var picture = NO_PICTURE
        var pictureDeserialized = false
        var thumbnail = NO_PICTURE_THUMBNAIL
        var thumbnailDeserialized = false
        var duration = UNKNOWN_DURATION
        var scores = HashSet<MetaDataProviderScoreValue>()
        var synonyms = HashSet<Title>()
        var synonymsDeserialized = false
        var studios = HashSet<String>()
        var studiosDeserialized = false
        var producers = HashSet<String>()
        var producersDeserialized = false
        var relatedAnime = HashSet<URI>()
        var relatedAnimeDeserialized = false
        var tags = HashSet<Tag>()
        var tagsDeserialized = false

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "sources" -> {
                    sources = uriHashSetAdapter.fromJson(reader)
                    sourcesDeserialized = true
                }
                "title" -> {
                    title = titleAdapter.fromJson(reader)
                    titleDeserialized = true
                }
                "type" -> {
                    type = typeAdapter.fromJson(reader)
                    typeDeserialized = true
                }
                "episodes" -> {
                    episodes = reader.nextInt()
                    episodesDeserialized = true
                }
                "status" -> {
                    status = statusAdapter.fromJson(reader)
                    statusDeserialized = true
                }
                "animeSeason" -> {
                    animeSeason = animeSeasonAdapter.fromJson(reader)
                    animeSeasonDeserialized = true
                }
                "picture" -> {
                    picture = uriAdapter.fromJson(reader)
                    pictureDeserialized = true
                }
                "thumbnail" -> {
                    thumbnail = uriAdapter.fromJson(reader)
                    thumbnailDeserialized = true
                }
                "duration" -> {
                    duration = durationAdapter.fromJson(reader)
                }
                "scores" -> {
                    scores = metaDataProviderScoreValueAdapter.fromJson(reader)
                }
                "synonyms" -> {
                    synonyms = titleHashSetAdapter.fromJson(reader)
                    synonymsDeserialized = true
                }
                "studios" -> {
                    studios = titleHashSetAdapter.fromJson(reader)
                    studiosDeserialized = true
                }
                "producers" -> {
                    producers = titleHashSetAdapter.fromJson(reader)
                    producersDeserialized = true
                }
                "relatedAnime" -> {
                    relatedAnime = uriHashSetAdapter.fromJson(reader)
                    relatedAnimeDeserialized = true
                }
                "tags" -> {
                    tags = tagHashSetAdapter.fromJson(reader)
                    tagsDeserialized = true
                }
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        when {
            !titleDeserialized -> throw IllegalStateException("Property 'title' is either missing or null.")
            !sourcesDeserialized -> throw IllegalStateException("Property 'sources' is either missing or null.")
            !episodesDeserialized -> throw IllegalStateException("Property 'episodes' is either missing or null.")
            !typeDeserialized -> throw IllegalStateException("Property 'type' is either missing or null.")
            !statusDeserialized -> throw IllegalStateException("Property 'status' is either missing or null.")
            !animeSeasonDeserialized -> throw IllegalStateException("Property 'animeSeason' is either missing or null.")
            !pictureDeserialized -> throw IllegalStateException("Property 'picture' is either missing or null.")
            !thumbnailDeserialized -> throw IllegalStateException("Property 'thumbnail' is either missing or null.")
            !synonymsDeserialized -> throw IllegalStateException("Property 'synonyms' is either missing or null.")
            !studiosDeserialized -> throw IllegalStateException("Property 'studios' is either missing or null.")
            !producersDeserialized -> throw IllegalStateException("Property 'producers' is either missing or null.")
            !relatedAnimeDeserialized -> throw IllegalStateException("Property 'relatedAnime' is either missing or null.")
            !tagsDeserialized -> throw IllegalStateException("Property 'tags' is either missing or null.")
        }

        return AnimeRaw(
            _title = title,
            _sources = sources,
            type = type,
            episodes = episodes,
            status = status,
            animeSeason = animeSeason,
            picture = picture,
            thumbnail = thumbnail,
            duration = duration,
            _synonyms = synonyms,
            _studios = studios,
            _producers = producers,
            _relatedAnime = relatedAnime,
            _tags = tags,
            activateChecks = false,
        ).addScores(scores)
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: AnimeRaw?) {
        requireNotNull(value) { "AnimeRawAdapter expects non-nullable value, but received null." }

        if (!value.activateChecks) {
            value.performChecks()
        }

        writer.beginObject()

        writer.name("sources")
        uriHashSetAdapter.toJson(writer, value.sources)

        writer.name("title")
        titleAdapter.toJson(writer, value.title)

        writer.name("type")
        typeAdapter.toJson(writer, value.type)

        writer.name("episodes").value(value.episodes)

        writer.name("status")
        statusAdapter.toJson(writer, value.status)

        writer.name("animeSeason")
        animeSeasonAdapter.toJson(writer, value.animeSeason)

        writer.name("picture")
        uriAdapter.toJson(writer, value.picture)

        writer.name("thumbnail")
        uriAdapter.toJson(writer, value.thumbnail)

        if (value.duration.duration != 0 || (value.duration.duration == 0 && writer.serializeNulls)) {
            writer.name("duration")
            durationAdapter.toJson(writer, value.duration)
        }

        writer.name("scores")
        metaDataProviderScoreValueAdapter.toJson(writer, value.scores)

        writer.name("synonyms")
        titleHashSetAdapter.toJson(writer, value.synonyms)

        writer.name("studios")
        studioHashSetAdapter.toJson(writer, value.studios)

        writer.name("producers")
        producerHashSetAdapter.toJson(writer, value.producers)

        writer.name("relatedAnime")
        uriHashSetAdapter.toJson(writer, value.relatedAnime)

        writer.name("tags")
        tagHashSetAdapter.toJson(writer, value.tags)

        writer.endObject()
    }
}