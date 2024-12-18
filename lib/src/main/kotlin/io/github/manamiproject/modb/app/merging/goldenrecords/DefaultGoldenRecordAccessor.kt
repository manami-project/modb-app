package io.github.manamiproject.modb.app.merging.goldenrecords

import io.github.manamiproject.modb.animeplanet.AnimePlanetConfig
import io.github.manamiproject.modb.app.extensions.firstNotNullResult
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.normalize
import io.github.manamiproject.modb.core.extensions.remove
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Title
import java.net.URI
import java.util.*

/**
 * Manages the golden record list in-memory.
 * There is no check for duplicates here.
 * @since 1.0.0
 */
class DefaultGoldenRecordAccessor: GoldenRecordAccessor {

    /** Outer map key = first two chars of the title. Inner map key = lower case, cleaned-up title */
    private val partitionedTitleCluster: HashMap<String, HashMap<Title, HashSet<UUID>>>  = hashMapOf()
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
        var entries = findByTitle(anime.title)

        // anime-planet usually provides a title which is very unique across all meta data provider. In case we couldn't find anything using that title we retry using the first synonym
        if (entries.isEmpty() && anime.sources.size == 1 && anime.sources.first().host == AnimePlanetConfig.hostname() && anime.synonyms.isNotEmpty()) {
            entries = findByTitle(anime.synonyms.first())
        }

        return entries
    }

    private fun findByTitle(title: Title): Set<PotentialGoldenRecord> {
        return fromPartitionedTitleCluster(title)[cleanupTitle(title)]
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

    override fun merge(goldenRecordId: UUID, anime: Anime): Anime {
        goldenRecords[goldenRecordId] = goldenRecords[goldenRecordId]?.mergeWith(anime) ?: throw IllegalStateException("Unable to find golden record [$goldenRecordId]")

        updateTitleCluster(goldenRecordId, anime)
        updateSourceCluster(goldenRecordId, anime)

        return goldenRecords[goldenRecordId]!!
    }

    override fun allEntries(): List<Anime> = goldenRecords.values.toList()

    override fun clear() {
        partitionedTitleCluster.clear()
        sourceCluster.clear()
        goldenRecords.clear()
    }

    private fun fromPartitionedTitleCluster(title: String): HashMap<Title, HashSet<UUID>> {
        val cleanedTitle = cleanupTitle(title)
        val partition = cleanedTitle.safeSubstring(0, 2)
        return partitionedTitleCluster[partition] ?: hashMapOf()
    }

    private fun updateTitleCluster(goldenRecordId: UUID, anime: Anime) {
        extractAllTitles(anime).forEach { title ->
            val partition = title.safeSubstring(0, 2)

            if (partitionedTitleCluster[partition] == null) {
                partitionedTitleCluster[partition] = hashMapOf()
            }

            val titleClusterEntry = partitionedTitleCluster[partition]?.get(title)

            if (titleClusterEntry == null) {
                partitionedTitleCluster[partition]?.set(title, hashSetOf(goldenRecordId))
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

    private fun cleanupTitle(title: String): String {
        return title.lowercase()
            .remove("!")
            .remove("¡")
            .remove("！")
            .remove("?")
            .remove("¿")
            .remove("？")
            .remove("#")
            .remove("＃")
            .remove("%")
            .remove("％")
            .remove("*")
            .remove("＊")
            .remove("+")
            .remove("＋")
            .remove(",")
            .remove("，")
            .remove("、")
            .remove(".")
            .remove("．")
            .remove("｡")
            .remove("・")
            .remove("。")
            .remove("/")
            .remove("／")
            .remove("∕")
            .remove("⁄")
            .remove("\\")
            .remove("=")
            .remove("＝")
            .remove("@")
            .remove("＠")
            .remove("|")
            .remove("｜")
            .remove("$")
            .remove("＄")
            .remove("￥")
            .remove("¥")
            .remove("¢")
            .remove("□")
            .remove("▲")
            .remove("△")
            .remove("▼")
            .remove("▽")
            .remove("◆")
            .remove("◇")
            .remove("○")
            .remove("◎")
            .remove("●")
            .remove("◯")
            .remove("★")
            .remove("☆")
            .remove("☠")
            .remove("♀")
            .remove("♂")
            .remove("♡")
            .remove("♥")
            .remove("♪")
            .remove("♭")
            .remove("♯")
            .remove("⚡")
            .remove("✞")
            .remove("✩")
            .remove("✶")
            .remove("✽")
            .remove("✿")
            .remove("❄")
            .remove("❤")
            .remove("⤴")
            .remove("←")
            .remove("↑")
            .remove("→")
            .remove("⇔")
            .remove("℃")
            .remove("‼")
            .remove("⁈")
            .remove("※")
            .remove("®")
            .remove("√")
            .remove("…")
            .remove("ª")
            .remove("ⁿ")
            .remove("･")
            .remove("·")
            .remove("‧")
            .remove("､")
            .remove("␣")
            .remove("∞")
            .remove("∅")
            .remove("∀")
            .remove("ː")
            .remove("±")
            .remove("°")
            .remove("•")
            .remove("†")

            // remove quotation marks and diacritics
            .remove("\"")
            .remove("'")
            .remove("^")
            .remove("`")
            .remove("’")
            .remove("‘")
            .remove("“")
            .remove("”")
            .remove("„")
            .remove("″")
            .remove("〝")
            .remove("〟")
            .remove("´")
            .remove("ʼ")
            .remove("‚")

            // remove colons
            .remove(":")
            .remove("：")
            .remove(";")
            .remove("；")

            // remove dashes and underscores
            .remove("-")
            .remove("_")
            .remove("—")
            .remove("―")
            .remove("–")
            .remove("＿")
            .remove("‐")
            .remove("‑")
            .remove("‒")
            .remove("−")
            .remove("─")
            .remove("ー️")
            .remove("－")
            .remove("ｰ")

            // remove tilde
            .remove("~")
            .remove("∽")
            .remove("～")
            .remove("〜")

            // remove opening and closing special chars
            .remove("<")
            .remove(">")
            .remove("＜")
            .remove("＞")
            .remove("(")
            .remove(")")
            .remove("（")
            .remove("）")
            .remove("[")
            .remove("]")
            .remove("［")
            .remove("］")
            .remove("{")
            .remove("}")
            .remove("｛")
            .remove("｝")
            .remove("｢")
            .remove("｣")
            .remove("「")
            .remove("」")
            .remove("〈")
            .remove("〉")
            .remove("《")
            .remove("》")
            .remove("『")
            .remove("』")
            .remove("【")
            .remove("】")
            .remove("〔")
            .remove("〕")
            .remove("≪")
            .remove("≫")
            .remove("«")
            .remove("»")
            .remove("≦")
            .remove("≧")
            .remove("≠")
            .remove("∬")

            // normalize special chars
            .replace("＆", "&")
            .replace("☓", "x")
            .replace("×", "x")
            .replace("ß", "ss")

            // symbols to latin character equivalent
            .replace("ⅰ", "i")
            .replace("ⅱ", "ii")
            .replace("ⅲ", "iii")
            .replace("ⅳ", "iv")
            .replace("ⅴ", "v")
            .replace("ⅵ", "vi")
            .replace("ⅶ", "vii")
            .replace("ⅺ", "xi")
            .replace("½", "12")
            .replace("⅙", "16")
            .replace("⅛", "18")
            .replace("²", "2")
            .replace("³", "3")
            .replace("№", "no")
            .replace("②", "2")
            .replace("⑤", "5")

            // zenkaku
            .replace("ａ", "a")
            .replace("ｂ", "b")
            .replace("ｃ", "c")
            .replace("ｄ", "d")
            .replace("ｅ", "e")
            .replace("ｆ", "f")
            .replace("ｇ", "g")
            .replace("ｈ", "h")
            .replace("ｉ", "i")
            .replace("ｊ", "j")
            .replace("ｋ", "k")
            .replace("ｌ", "l")
            .replace("ｍ", "m")
            .replace("ｎ", "n")
            .replace("ｏ", "o")
            .replace("ｐ", "p")
            .replace("ｑ", "q")
            .replace("ｒ", "r")
            .replace("ｓ", "s")
            .replace("ｔ", "t")
            .replace("ｕ", "u")
            .replace("ｖ", "v")
            .replace("ｗ", "w")
            .replace("ｘ", "x")
            .replace("ｙ", "y")
            .replace("ｚ", "z")
            .replace("０", "0")
            .replace("１", "1")
            .replace("２", "2")
            .replace("３", "3")
            .replace("４", "4")
            .replace("５", "5")
            .replace("６", "6")
            .replace("７", "7")
            .replace("８", "8")
            .replace("９", "9")

            // normalize whitespaces
            .normalize()
            .trim()
            .replace(" ", EMPTY)
    }

    companion object {
        /**
         * Singleton of [DefaultGoldenRecordAccessor]
         * @since 1.0.0
         */
        val instance: DefaultGoldenRecordAccessor by lazy { DefaultGoldenRecordAccessor() }
    }
}

private fun String.safeSubstring(start: Int, end: Int): String {
    val checkedEnd = if (end > this.length) {
        this.length
    } else {
        end
    }
    return this.substring(start, checkedEnd)
}