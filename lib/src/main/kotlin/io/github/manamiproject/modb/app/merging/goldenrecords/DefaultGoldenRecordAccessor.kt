package io.github.manamiproject.modb.app.merging.goldenrecords

import io.github.manamiproject.modb.animeplanet.AnimePlanetConfig
import io.github.manamiproject.modb.app.extensions.firstNotNullResult
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Title
import java.net.URI
import java.util.*
import kotlin.collections.HashSet

/**
 * Manages the golden record list in-memory.
 * There is no check for duplicates here.
 * @since 1.0.0
 */
class DefaultGoldenRecordAccessor : GoldenRecordAccessor {

    /** key = lower case, cleaned-up title */
    private val titleCluster: HashMap<Title, HashSet<UUID>>  = hashMapOf()
    private val sourceCluster: HashMap<URI, UUID>  = hashMapOf()
    private val goldenRecords: HashMap<UUID, Anime>  = hashMapOf()

    override fun createGoldenRecord(anime: Anime) {
        val uuid = UUID.randomUUID()
        goldenRecords[uuid] = anime

        updateTitleCluster(uuid, anime)
        updateSourceCluster(uuid, anime)
    }

    override fun findGoldenRecordBySource(sources: Set<URI>): PotentialGoldenRecord? {
        sourceCluster.firstNotNullResult(sources)?.let { uuid ->
            goldenRecords[uuid]?.let { anime ->
                return PotentialGoldenRecord(uuid, anime)
            }
        }

        return null
    }

    override fun findPossibleGoldenRecords(anime: Anime): Set<PotentialGoldenRecord> {
        val findByTitle: (Title) -> Set<PotentialGoldenRecord> = { title ->
            titleCluster[cleanupTitle(title)]
                ?.asSequence()
                ?.map { Pair(it, goldenRecords[it]) }
                ?.filter { it.second != null }
                ?.map {
                    PotentialGoldenRecord(
                        id = it.first,
                        anime = it.second!!
                    )
                }
                ?.toSet()
                ?: emptySet()
        }

        var entries = findByTitle(anime.title)

        // anime-planet usually provides a title which is very unique across all meta data provider. In case we couldn't find anything using that title we try the first synonym
        if (entries.isEmpty() && anime.sources.size == 1 && anime.sources.first().host == AnimePlanetConfig.hostname() && anime.synonyms.isNotEmpty()) {
            entries = findByTitle(anime.synonyms.first())
        }

        return entries
    }

    override fun merge(goldenRecordId: UUID, anime: Anime): Anime {
        goldenRecords[goldenRecordId] = goldenRecords[goldenRecordId]?.mergeWith(anime) ?: throw IllegalStateException("Unable to find golden record [$goldenRecordId]")

        updateTitleCluster(goldenRecordId, anime)
        updateSourceCluster(goldenRecordId, anime)

        return goldenRecords[goldenRecordId]!!
    }

    override fun allEntries(): List<Anime> = goldenRecords.values.toList()

    private fun updateTitleCluster(goldenRecordId: UUID, anime: Anime) {
        extractAllTitles(anime).forEach { title ->
            val titleClusterEntry = titleCluster[title]

            if (titleClusterEntry == null) {
                titleCluster[title] = hashSetOf(goldenRecordId)
            } else {
                titleClusterEntry.add(goldenRecordId)
            }
        }
    }

    private fun extractAllTitles(anime: Anime) = listOf(anime.title).union(anime.synonyms).map { cleanupTitle(it) }

    private fun updateSourceCluster(goldenRecordId: UUID, anime: Anime) {
        anime.sources.forEach {
            sourceCluster[it] = goldenRecordId
        }
    }

    /**
     * The following characters musn't  be replaced, because they are far too often indicator for different seasons: ' ? !
     */
    private fun cleanupTitle(title: String): String {
        return title.replace(Regex(" ?☆ ?"), " ")
            .replace(Regex(" ?♪ ?"), " ")
            .replace(Regex(" ?★ ?"), " ")
            .replace(Regex(" ?✩ ?"), " ")
            .replace(Regex(" ?♥ ?"), " ")
            .replace(Regex(" ?♡ ?"), " ")

            // remove colons
            .replace(Regex(" ?: ?"), " ")
            .replace(Regex(" ?： ?"), " ")

            // remove dashes and underscores
            .replace(Regex(" ?- ?"), " ")
            .replace(Regex(" ?_ ?"), " ")
            .replace(Regex(" ?— ?"), " ")
            .replace(Regex(" ?― ?"), " ")
            .replace(Regex(" ?– ?"), " ")
            .replace(Regex(" ?＿ ?"), " ")

            // remove tilde
            .replace(Regex(" ?~ ?"), " ")
            .replace(Regex(" ?∽ ?"), " ")
            .replace(Regex(" ?～ ?"), " ")
            .replace(Regex(" ?〜 ?"), " ")

            // remove opening and closing special chars
            .replace(Regex(" ?」 ?"), " ")
            .replace(Regex(" ?「 ?"), " ")
            .replace(Regex(" ?《 ?"), " ")
            .replace(Regex(" ?》 ?"), " ")
            .replace(Regex(" ?\" ?"), " ")
            .replace(Regex(" ?“ ?"), " ")
            .replace(Regex(" ?” ?"), " ")
            .replace(Regex(" ?\\( ?"), " ")
            .replace(Regex(" ?\\) ?"), " ")
            .replace(Regex(" ?\\[ ?"), " ")
            .replace(Regex(" ?\\] ?"), " ")
            .replace(Regex(" ?> ?"), " ")
            .replace(Regex(" ?< ?"), " ")
            .replace(Regex(" ?『 ?"), " ")
            .replace(Regex(" ?』 ?"), " ")
            .replace(Regex(" ?\\{ ?"), " ")
            .replace(Regex(" ?\\} ?"), " ")

            // noramlize strange special chars
            .replace(Regex("＆"), "&")
            .replace(Regex("？"), "?")

            // normalize whitespaces
            .replace(Regex(" {2,}"), " ")
            .trim()
            .lowercase()
    }

    companion object {
        /**
         * Singleton of [DefaultGoldenRecordAccessor]
         * @since 1.0.0
         */
        val instance: DefaultGoldenRecordAccessor by lazy { DefaultGoldenRecordAccessor() }
    }
}