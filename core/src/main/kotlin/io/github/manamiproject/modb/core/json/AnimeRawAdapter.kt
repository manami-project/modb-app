package io.github.manamiproject.modb.core.json

import com.squareup.moshi.*
import io.github.manamiproject.modb.core.anime.AnimeRaw
import io.github.manamiproject.modb.core.anime.AnimeRaw.Companion.NO_PICTURE
import io.github.manamiproject.modb.core.anime.AnimeRaw.Companion.NO_PICTURE_THUMBNAIL
import io.github.manamiproject.modb.core.anime.AnimeSeason
import io.github.manamiproject.modb.core.anime.Tag
import io.github.manamiproject.modb.core.anime.Title
import io.github.manamiproject.modb.core.extensions.EMPTY
import java.net.URI
import io.github.manamiproject.modb.core.anime.AnimeStatus.UNKNOWN as UNKNOWN_STATUS
import io.github.manamiproject.modb.core.anime.AnimeType.UNKNOWN as UNKNOWN_TYPE
import io.github.manamiproject.modb.core.anime.Duration.Companion.UNKNOWN as UNKNOWN_DURATION

internal class AnimeRawAdapter: JsonAdapter<AnimeRaw>() {

    private val titleAdapter = TitleAdapter()
    private val uriHashSetAdapter = HashSetAdapter(UriAdapter())
    private val titleHashSetAdapter = HashSetAdapter(TitleAdapter())
    private val uriAdapter = UriAdapter()
    private val tagHashSetAdapter = HashSetAdapter(TagAdapter())
    private val typeAdapter = AnimeTypeAdapter()
    private val statusAdapter = AnimeStatusAdapter()
    private val durationAdapter = DurationAdapter()
    private val animeSeasonAdapter = AnimeSeasonAdapter()


    @FromJson
    override fun fromJson(reader: JsonReader): AnimeRaw {
        reader.beginObject()

        var title = EMPTY
        var titleDeserialized = false
        var sources = HashSet<URI>()
        var sourcesDeserialized = false
        var type = UNKNOWN_TYPE
        var typeDeserialized = false
        var synonyms = HashSet<Title>()
        var synonymsDeserialized = false
        var episodes = 0
        var episodesDeserialized = false
        var status = UNKNOWN_STATUS
        var statusDeserialized = false
        var picture = NO_PICTURE
        var pictureDeserialized = false
        var thumbnail = NO_PICTURE_THUMBNAIL
        var thumbnailDeserialized = false
        var duration = UNKNOWN_DURATION
        var tags = HashSet<Tag>()
        var tagsDeserialized = false
        var relatedAnime = HashSet<URI>()
        var relatedAnimeDeserialized = false
        var animeSeason = AnimeSeason()
        var animeSeasonDeserialized = false

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
                "synonyms" -> {
                    synonyms = titleHashSetAdapter.fromJson(reader)
                    synonymsDeserialized = true
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
            !typeDeserialized -> throw IllegalStateException("Property 'type' is either missing or null.")
            !synonymsDeserialized -> throw IllegalStateException("Property 'synonyms' is either missing or null.")
            !episodesDeserialized -> throw IllegalStateException("Property 'episodes' is either missing or null.")
            !statusDeserialized -> throw IllegalStateException("Property 'status' is either missing or null.")
            !pictureDeserialized -> throw IllegalStateException("Property 'picture' is either missing or null.")
            !thumbnailDeserialized -> throw IllegalStateException("Property 'thumbnail' is either missing or null.")
            !tagsDeserialized -> throw IllegalStateException("Property 'tags' is either missing or null.")
            !relatedAnimeDeserialized -> throw IllegalStateException("Property 'relatedAnime' is either missing or null.")
            !animeSeasonDeserialized -> throw IllegalStateException("Property 'animeSeason' is either missing or null.")
        }

        return AnimeRaw(
            _title = title,
            _sources = sources,
            _synonyms = synonyms,
            type = type,
            episodes = episodes,
            status = status,
            picture = picture,
            thumbnail = thumbnail,
            _tags = tags,
            _relatedAnime = relatedAnime,
            duration = duration,
            animeSeason = animeSeason,
            activateChecks = false,
        )
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: AnimeRaw?) {
        requireNotNull(value) { "AnimeRawAdapter expects non-nullable value, but received null." }

        if (!value.activateChecks) {
            value.performChecks()
        }

        writer.beginObject()

        writer.name("sources").beginArray()
        value.sources.map { it.toString() }.sorted().forEach { writer.value(it) }
        writer.endArray()

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

        writer.name("synonyms").beginArray()
        value.synonyms.sorted().forEach { writer.value(it) }
        writer.endArray()

        writer.name("relatedAnime").beginArray()
        value.relatedAnime.map { it.toString() }.sorted().forEach { writer.value(it) }
        writer.endArray()

        writer.name("tags").beginArray()
        value.tags.sorted().forEach { writer.value(it) }
        writer.endArray()

        writer.endObject()
    }
}