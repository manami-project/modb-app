package io.github.manamiproject.modb.app.merging.goldenrecords

import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Anime.Type.*
import io.github.manamiproject.modb.core.models.Duration
import io.github.manamiproject.modb.core.models.Duration.TimeUnit.MINUTES
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import java.net.URI
import java.util.*
import kotlin.test.Test

internal class DefaultGoldenRecordAccessorTest {

    @Nested
    inner class CreateGoldenRecordTests {

        @Test
        fun `create a new golden record`() {
            // given
            val goldenRecordList = DefaultGoldenRecordAccessor()

            val anime = Anime("Death Note")

            // when
            goldenRecordList.createGoldenRecord(anime)

            // then
            assertThat(goldenRecordList.allEntries()).containsExactly(anime)
        }

        @Test
        fun `creating a new golden record based on the exact same anime will create another new golden record, because there is no duplicate check`() {
            // given
            val goldenRecordList = DefaultGoldenRecordAccessor()

            val anime = Anime("Death Note")
            goldenRecordList.createGoldenRecord(anime)

            // when
            goldenRecordList.createGoldenRecord(anime)

            // then
            assertThat(goldenRecordList.allEntries()).hasSize(2)
            assertThat(goldenRecordList.allEntries()).containsExactly(anime, anime)
        }
    }

    @Nested
    inner class FindGoldenRecordBySources {

        @Test
        fun `returns null, because a golden record does not exist for that source`() {
            // given
            val goldenRecordList = DefaultGoldenRecordAccessor()

            // when
            val result = goldenRecordList.findGoldenRecordBySource(hashSetOf(URI("https://myanimelist.net/anime/1535")))

            // then
            assertThat(result).isNull()
        }

        @Test
        fun `finds golden record by a single source`() {
            // given
            val goldenRecordList = DefaultGoldenRecordAccessor()

            val anime = Anime(
                _sources = hashSetOf(
                    URI("https://myanimelist.net/anime/37514"),
                ),
                _title = "Made in Abyss Movie 1: Tabidachi no Yoake",
                type = MOVIE,
                episodes = 1,
                picture = URI("https://cdn.myanimelist.net/images/anime/1173/95167.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1173/95167t.jpg"),
                synonyms = hashSetOf(
                    "Made in Abyss Movie",
                    "Made in Abyss Movie 1: Journey's Dawn",
                    "劇場版総集編【前編】メイドインアビス 旅立ちの夜明け",
                ),
                relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/34599"),
                    URI("https://myanimelist.net/anime/37515"),
                )
            )

            goldenRecordList.createGoldenRecord(anime)

            // when
            val result = goldenRecordList.findGoldenRecordBySource(anime.sources)!!

            // then
            assertThat(result.anime).isEqualTo(anime)
        }

        @Test
        fun `finds golden record by multiple sources where there is only a golden record for the last source`() {
            // given
            val goldenRecordList = DefaultGoldenRecordAccessor()

            val anime = Anime(
                _sources = hashSetOf(
                    URI("https://myanimelist.net/anime/37514"),
                ),
                _title = "Made in Abyss Movie 1: Tabidachi no Yoake",
                type = MOVIE,
                episodes = 1,
                picture = URI("https://cdn.myanimelist.net/images/anime/1173/95167.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1173/95167t.jpg"),
                synonyms = hashSetOf(
                    "Made in Abyss Movie",
                    "Made in Abyss Movie 1: Journey's Dawn",
                    "劇場版総集編【前編】メイドインアビス 旅立ちの夜明け",
                ),
                relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/34599"),
                    URI("https://myanimelist.net/anime/37515"),
                )
            )

            goldenRecordList.createGoldenRecord(anime)

            // when
            val result = goldenRecordList.findGoldenRecordBySource(
                hashSetOf(
                    URI("https://anilist.co/anime/101343"),
                    URI("https://kitsu.app/anime/41080"),
                    URI("https://notify.moe/anime/8tIKhKimg"),
                    anime.sources.first(),
                )
            )!!

            // then
            assertThat(result.anime).isEqualTo(anime)
        }
    }

    @Nested
    inner class FindPossibleGoldenRecordsTests {

        @Test
        fun `return empty list for matching episode, but non-matching main title`() {
            // given
            val anime = Anime(
                _title = "Made in Abyss",
                episodes = 13,
            )

            val animeToFindGoldenRecordFor = Anime(
                _title = "Searching for",
                episodes = 13,
            )

            val goldenRecordList = DefaultGoldenRecordAccessor()
            goldenRecordList.createGoldenRecord(anime)

            // when
            val result = goldenRecordList.findPossibleGoldenRecords(animeToFindGoldenRecordFor)

            // then
            assertThat(result).isEmpty()
        }

        @Test
        fun `find correct entry for matching main title`() {
            // given
            val madeInAbyss = Anime(
                _sources = hashSetOf(
                    URI("https://myanimelist.net/anime/34599"),
                ),
                _title = "Made in Abyss",
                type = TV,
                episodes = 13,
                picture = URI("https://cdn.myanimelist.net/images/anime/6/86733.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/6/86733t.jpg"),
                synonyms = hashSetOf(
                    "メイドインアビス",
                ),
                relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/36862"),
                    URI("https://myanimelist.net/anime/37514"),
                    URI("https://myanimelist.net/anime/37515"),
                ),
            )

            val madeInAbyssMovie = Anime(
                _sources = hashSetOf(
                    URI("https://myanimelist.net/anime/37514"),
                ),
                _title = "Made in Abyss Movie 1: Tabidachi no Yoake",
                type = MOVIE,
                episodes = 1,
                picture = URI("https://cdn.myanimelist.net/images/anime/1173/95167.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1173/95167t.jpg"),
                synonyms = hashSetOf(
                    "Made in Abyss Movie 1: Journey's Dawn",
                    "劇場版総集編【前編】メイドインアビス 旅立ちの夜明け",
                ),
                relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/34599"),
                    URI("https://myanimelist.net/anime/37515"),
                ),
            )

            val soraYoriMoTooiBasho = Anime(
                _sources = hashSetOf(
                    URI("https://myanimelist.net/anime/35839"),
                ),
                _title = "Sora yori mo Tooi Basho",
                type = TV,
                episodes = 13,
                picture = URI("https://cdn.myanimelist.net/images/anime/6/89879.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/6/89879t.jpg"),
                synonyms = hashSetOf(
                    "A Place Further Than The Universe",
                    "Uchuu yori mo Tooi Basho",
                    "A Story That Leads to the Antarctica",
                    "Yorimoi",
                    "宇宙よりも遠い場所",
                ),
                relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/37123"),
                ),
            )

            val soraYoriMoTooiBashoSpecial = Anime(
                _sources = hashSetOf(
                    URI("https://myanimelist.net/anime/37123")
                ),
                _title = "Sora yori mo Tooi Basho: Yokoku",
                type = ONA,
                episodes = 13,
                picture = URI("https://cdn.myanimelist.net/images/anime/1897/98785.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1897/98785t.jpg"),
                synonyms = hashSetOf(
                    "Sora yori mo Tooi Basho next episode previews",
                    "宇宙よりも遠い場所 予告 WEB限定",
                ),
                relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/35839"),
                ),
            )

            val animeToFindGoldenRecordFor = Anime(
                _sources = hashSetOf(
                    URI("https://anilist.co/anime/97986"),
                ),
                _title = "Made in Abyss",
                type = TV,
                episodes = 13,
                picture = URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx97986-fzJBML9qecb4.jpg"),
                thumbnail = URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx97986-fzJBML9qecb4.jpg"),
            )

            val goldenRecordList = DefaultGoldenRecordAccessor()
            goldenRecordList.createGoldenRecord(madeInAbyss)
            goldenRecordList.createGoldenRecord(madeInAbyssMovie)
            goldenRecordList.createGoldenRecord(soraYoriMoTooiBasho)
            goldenRecordList.createGoldenRecord(soraYoriMoTooiBashoSpecial)

            // when
            val result = goldenRecordList.findPossibleGoldenRecords(animeToFindGoldenRecordFor)

            // then
            assertThat(result.size).isOne()
            assertThat(result.first().anime).isEqualTo(madeInAbyss)
        }

        @Test
        fun `find correct entry for matching title which is shorter than the partition size`() {
            // given
            val animeWithTitleShorterThanPartitionSize = Anime(
                _sources = hashSetOf(
                    URI("https://example.org.net/anime/y"),
                ),
                _title = "Y",
            )

            val soraYoriMoTooiBasho = Anime(
                _sources = hashSetOf(
                    URI("https://myanimelist.net/anime/35839"),
                ),
                _title = "Sora yori mo Tooi Basho",
                type = TV,
                episodes = 13,
                picture = URI("https://cdn.myanimelist.net/images/anime/6/89879.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/6/89879t.jpg"),
                synonyms = hashSetOf(
                    "A Place Further Than The Universe",
                    "Uchuu yori mo Tooi Basho",
                    "A Story That Leads to the Antarctica",
                    "Yorimoi",
                    "宇宙よりも遠い場所",
                ),
                relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/37123"),
                ),
            )

            val animeToFindGoldenRecordFor = Anime(
                _sources = hashSetOf(
                    URI("https://otherexample.com/anime/994"),
                ),
                _title = "Y",
            )

            val goldenRecordList = DefaultGoldenRecordAccessor()
            goldenRecordList.createGoldenRecord(animeWithTitleShorterThanPartitionSize)
            goldenRecordList.createGoldenRecord(soraYoriMoTooiBasho)

            // when
            val result = goldenRecordList.findPossibleGoldenRecords(animeToFindGoldenRecordFor)

            // then
            assertThat(result.size).isOne()
            assertThat(result.first().anime).isEqualTo(animeWithTitleShorterThanPartitionSize)
        }

        @ParameterizedTest
        @ValueSource(strings = [
            "!",
            "¡",
            "！",
            "?",
            "¿",
            "？",
            "#",
            "＃",
            "%",
            "％",
            "*",
            "＊",
            "+",
            "＋",
            ",",
            "，",
            "、",
            ".",
            "．",
            "｡",
            "・",
            "。",
            "/",
            "／",
            "∕",
            "⁄",
            "\\",
            "=",
            "＝",
            "@",
            "＠",
            "|",
            "｜",
            "$",
            "＄",
            "￥",
            "¥",
            "¢",
            "□",
            "▲",
            "△",
            "▼",
            "▽",
            "◆",
            "◇",
            "○",
            "◎",
            "●",
            "◯",
            "★",
            "☆",
            "☠",
            "♀",
            "♂",
            "♡",
            "♥",
            "♪",
            "♭",
            "♯",
            "⚡",
            "✞",
            "✩",
            "✶",
            "✽",
            "✿",
            "❄",
            "❤",
            "⤴",
            "←",
            "↑",
            "→",
            "⇔",
            "℃",
            "‼",
            "⁈",
            "※",
            "®",
            "√",
            "…",
            "ª",
            "ⁿ",
            "･",
            "·",
            "‧",
            "､",
            "␣",
            "∞",
            "∅",
            "∀",
            "ː",
            "±",
            "°",
            "•",
            "†",
            "\"",
            "'",
            "^",
            "`",
            "’",
            "‘",
            "“",
            "”",
            "„",
            "″",
            "〝",
            "〟",
            "´",
            "ʼ",
            "‚",
            ":",
            "：",
            ";",
            "；",
            "-",
            "_",
            "—",
            "―",
            "–",
            "＿",
            "‐",
            "‑",
            "‒",
            "−",
            "─",
            "ー️",
            "－",
            "ｰ",
            "~",
            "∽",
            "～",
            "〜",
            "<",
            ">",
            "＜",
            "＞",
            "(",
            ")",
            "（",
            "）",
            "[",
            "]",
            "［",
            "］",
            "{",
            "}",
            "｛",
            "｝",
            "｢",
            "｣",
            "「",
            "」",
            "〈",
            "〉",
            "《",
            "》",
            "『",
            "』",
            "【",
            "】",
            "〔",
            "〕",
            "≪",
            "≫",
            "«",
            "»",
            "≦",
            "≧",
            "≠",
            "∬",
            "\u202F",
            "\u200A",
            "\u205F",
            "\u2000",
            "\u2001",
            "\u2002",
            "\u2003",
            "\u2004",
            "\u2005",
            "\u2006",
            "\u2007",
            "\u2008",
            "\u2009",
            "﻿",
            "᠎",
            "⁠",
            "‍",
            "",
            "‌",
            "​",
            "­",
            "",
            " ",
            "  ",
            "    ",
        ])
        fun `find title by ignoring special char`(specialChar: String) {
            // given
            val titleWithoutSpecialChar = Anime(
                _sources = hashSetOf(
                    URI("https://myanimelist.net/anime/37514"),
                ),
                _title = "Made in Abyss Movie Tabidachi no Yoake",
                type = MOVIE,
                episodes = 1,
                picture = URI("https://cdn.myanimelist.net/images/anime/1173/95167.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1173/95167t.jpg"),
                synonyms = hashSetOf(
                    "Made in Abyss Movie 1: Journey's Dawn",
                    "劇場版総集編【前編】メイドインアビス 旅立ちの夜明け",
                ),
                relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/34599"),
                    URI("https://myanimelist.net/anime/37515"),
                )
            )

            val titleWithSpecialChar = Anime(
                _sources = hashSetOf(
                    URI("https://example.org/anime/37514"),
                ),
                _title = "Made in Abyss Movie $specialChar Tabidachi no Yoake",
                type = MOVIE,
                episodes = 1,
                picture = URI("https://cdn.myanimelist.net/images/anime/1173/95167.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1173/95167t.jpg")
            )

            val goldenRecordList = DefaultGoldenRecordAccessor()
            goldenRecordList.createGoldenRecord(titleWithSpecialChar)

            // when
            val result = goldenRecordList.findPossibleGoldenRecords(titleWithoutSpecialChar)

            // then
            assertThat(result.size).isOne()
            assertThat(result.first().anime).isEqualTo(titleWithSpecialChar)
        }

        @ParameterizedTest
        @CsvSource(
            "＆,&",
            "☓,x",
            "×,x",
            "ß,ss",
            "ⅰ,i",
            "ⅱ,ii",
            "ⅲ,iii",
            "ⅳ,iv",
            "ⅴ,v",
            "ⅵ,vi",
            "ⅶ,vii",
            "ⅺ,xi",
            "½,12",
            "⅙,16",
            "⅛,18",
            "²,2",
            "³,3",
            "№,no",
            "②,2",
            "⑤,5",
            "ａ,a",
            "ｂ,b",
            "ｃ,c",
            "ｄ,d",
            "ｅ,e",
            "ｆ,f",
            "ｇ,g",
            "ｈ,h",
            "ｉ,i",
            "ｊ,j",
            "ｋ,k",
            "ｌ,l",
            "ｍ,m",
            "ｎ,n",
            "ｏ,o",
            "ｐ,p",
            "ｑ,q",
            "ｒ,r",
            "ｓ,s",
            "ｔ,t",
            "ｕ,u",
            "ｖ,v",
            "ｗ,w",
            "ｘ,x",
            "ｙ,y",
            "ｚ,z",
            "０,0",
            "１,1",
            "２,2",
            "３,3",
            "４,4",
            "５,5",
            "６,6",
            "７,7",
            "８,8",
            "９,9",
        )
        fun `returns entry, because it correctly replaces special chars`(specialChar: String, replacement: String) {
            // given
            val titleWithoutSpecialChar = Anime(
                _sources = hashSetOf(
                    URI("https://myanimelist.net/anime/37514"),
                ),
                _title = "Made in Abyss Movie $replacement Tabidachi no Yoake",
                type = MOVIE,
                episodes = 1,
                picture = URI("https://cdn.myanimelist.net/images/anime/1173/95167.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1173/95167t.jpg"),
                synonyms = hashSetOf(
                    "Made in Abyss Movie 1: Journey's Dawn",
                    "劇場版総集編【前編】メイドインアビス 旅立ちの夜明け",
                ),
                relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/34599"),
                    URI("https://myanimelist.net/anime/37515"),
                )
            )

            val titleWithSpecialChar = Anime(
                _sources = hashSetOf(
                    URI("https://example.org/anime/37514"),
                ),
                _title = "Made in Abyss Movie $specialChar Tabidachi no Yoake",
                type = MOVIE,
                episodes = 1,
                picture = URI("https://cdn.myanimelist.net/images/anime/1173/95167.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1173/95167t.jpg")
            )

            val goldenRecordList = DefaultGoldenRecordAccessor()
            goldenRecordList.createGoldenRecord(titleWithSpecialChar)

            // when
            val result = goldenRecordList.findPossibleGoldenRecords(titleWithoutSpecialChar)

            // then
            assertThat(result.size).isOne()
            assertThat(result.first().anime).isEqualTo(titleWithSpecialChar)
        }

        @Test
        fun `returns multiple possible golden records, because the anime share the same synonym`() {
            // given
            val madeInAbyssMovie = Anime(
                _sources = hashSetOf(
                    URI("https://myanimelist.net/anime/37514"),
                ),
                _title = "Made in Abyss Movie 1: Tabidachi no Yoake",
                type = MOVIE,
                episodes = 1,
                picture = URI("https://cdn.myanimelist.net/images/anime/1173/95167.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1173/95167t.jpg"),
                synonyms = hashSetOf(
                    "Made in Abyss Movie",
                    "Made in Abyss Movie 1: Journey's Dawn",
                    "劇場版総集編【前編】メイドインアビス 旅立ちの夜明け"
                ),
                relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/34599"),
                    URI("https://myanimelist.net/anime/37515"),
                ),
            )

            val madeInAbyssMovie2 = Anime(
                _sources = hashSetOf(
                    URI("https://myanimelist.net/anime/37515"),
                ),
                _title = "Made in Abyss Movie 2: Hourou Suru Tasogare",
                type = MOVIE,
                episodes = 1,
                picture = URI("https://cdn.myanimelist.net/images/anime/1336/95168.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1336/95168t.jpg"),
                synonyms = hashSetOf(
                    "Made in Abyss Movie",
                    "Made in Abyss Movie 2: Wandering Twilight",
                    "劇場版総集編【後編】メイドインアビス 放浪する黄昏",
                ),
                relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/34599"),
                    URI("https://myanimelist.net/anime/37515"),
                )
            )

            val soraYoriMoTooiBasho = Anime(
                _sources = hashSetOf(
                    URI("https://myanimelist.net/anime/35839"),
                ),
                _title = "Sora yori mo Tooi Basho",
                type = TV,
                episodes = 13,
                picture = URI("https://cdn.myanimelist.net/images/anime/6/89879.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/6/89879t.jpg"),
                synonyms = hashSetOf(
                    "A Place Further Than The Universe",
                    "Uchuu yori mo Tooi Basho",
                    "A Story That Leads to the Antarctica",
                    "Yorimoi",
                    "宇宙よりも遠い場所",
                ),
                relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/37123"),
                ),
            )

            val soraYoriMoTooiBashoSpecial = Anime(
                _sources = hashSetOf(
                    URI("https://myanimelist.net/anime/37123"),
                ),
                _title = "Sora yori mo Tooi Basho: Yokoku",
                type = ONA,
                episodes = 13,
                picture = URI("https://cdn.myanimelist.net/images/anime/1897/98785.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1897/98785t.jpg"),
                synonyms = hashSetOf(
                    "Sora yori mo Tooi Basho next episode previews",
                    "宇宙よりも遠い場所 予告 WEB限定",
                ),
                relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/35839"),
                )
            )

            val animeToFindGoldenRecordFor = Anime(
                _sources = hashSetOf(
                    URI("https://anilist.co/anime/97986"),
                ),
                _title = "Made in Abyss Movie",
                type = MOVIE,
                episodes = 1,
                picture = URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/nx101344-Cx5qshLvAo0j.jpg"),
                thumbnail = URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/nx101344-Cx5qshLvAo0j.jpg")
            )

            val goldenRecordList = DefaultGoldenRecordAccessor()
            goldenRecordList.createGoldenRecord(madeInAbyssMovie)
            goldenRecordList.createGoldenRecord(madeInAbyssMovie2)
            goldenRecordList.createGoldenRecord(soraYoriMoTooiBasho)
            goldenRecordList.createGoldenRecord(soraYoriMoTooiBashoSpecial)

            // when
            val result = goldenRecordList.findPossibleGoldenRecords(animeToFindGoldenRecordFor)

            // then
            val resultingRecords = result.map { it.anime }
            assertThat(result.size).isEqualTo(2)
            assertThat(resultingRecords).contains(madeInAbyssMovie)
            assertThat(resultingRecords).contains(madeInAbyssMovie2)
        }

        @Test
        fun `if nothing could be found for the title and the source is anime-planet then try the first synonym`() {
            // given
            val soraYoriMoTooiBasho = Anime(
                _sources = hashSetOf(
                    URI("https://myanimelist.net/anime/35839"),
                ),
                _title = "Sora yori mo Tooi Basho",
                type = TV,
                episodes = 13,
                picture = URI("https://cdn.myanimelist.net/images/anime/6/89879.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/6/89879t.jpg"),
                synonyms = hashSetOf(
                    "Uchuu yori mo Tooi Basho",
                    "A Story That Leads to the Antarctica",
                    "Yorimoi",
                    "宇宙よりも遠い場所",
                ),
                relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/37123"),
                ),
            )

            val animeToFindGoldenRecordFor = Anime(
                _sources = hashSetOf(
                    URI("https://anime-planet.com/anime/a-place-further-than-the-universe"),
                ),
                _title = "A Place Further Than the Universe",
                type = TV,
                episodes = 13,
                synonyms = hashSetOf(
                    "Sora yori mo Tooi Basho",
                ),
            )

            val goldenRecordList = DefaultGoldenRecordAccessor()
            goldenRecordList.createGoldenRecord(soraYoriMoTooiBasho)

            // when
            val result = goldenRecordList.findPossibleGoldenRecords(animeToFindGoldenRecordFor)

            // then
            assertThat(result.size).isOne()
            assertThat(result.first().anime).isEqualTo(soraYoriMoTooiBasho)
        }
    }

    @Nested
    inner class MergeTests {

        @Test
        fun `throws an exception if a golden record cannot be found for the given uuid`() {
            // given
            val id = UUID.randomUUID()
            val goldenRecordList = DefaultGoldenRecordAccessor()

            val anime = Anime(
                _title =  "Death Note",
                duration = Duration(25, MINUTES),
                synonyms = hashSetOf(
                    "Caderno da Morte",
                ),
            )

            // when
            val result = assertThrows<IllegalStateException> {
                goldenRecordList.merge(id, anime)
            }

            // then
            assertThat(result).hasMessage("Unable to find golden record [$id]")
        }

        @Test
        fun `adds title and synonyms of the other anime to the anime in the golden record`() {
            // given
            val goldenRecordList = DefaultGoldenRecordAccessor()

            val goldenAnime = Anime(
                _title =  "Death Note",
                duration = Duration(25, MINUTES),
                synonyms = hashSetOf(
                    "Caderno da Morte",
                ),
            )

            goldenRecordList.createGoldenRecord(goldenAnime)
            val uuid = goldenRecordList.findPossibleGoldenRecords(goldenAnime).first().id

            val otherAnime = Anime(
                _title =  "DEATH NOTE",
                duration = Duration(25, MINUTES),
                synonyms = hashSetOf(
                    "Caderno da Morte",
                    "Quaderno della Morte",
                ),
            )

            // when
            val result = goldenRecordList.merge(uuid, otherAnime)

            // then
            assertThat(result.title).isEqualTo(result.title)
            assertThat(result.synonyms).containsExactlyInAnyOrder(
                "Caderno da Morte",
                "DEATH NOTE",
                "Quaderno della Morte",
            )
        }

        @Test
        fun `merge related anime and source links`() {
            // given
            val goldenRecordList = DefaultGoldenRecordAccessor()

            val anime = Anime(
                _sources = hashSetOf(
                    URI("https://myanimelist.net/anime/1535"),
                ),
                _title = "Death Note",
                relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/2994"),
                ),
            )

            goldenRecordList.createGoldenRecord(anime)
            val uuid = goldenRecordList.findPossibleGoldenRecords(anime).first().id

            val otherAnime = Anime(
                _sources = hashSetOf(
                    URI("https://anidb.net/anime/4563"),
                ),
                _title = "Death Note",
                relatedAnime = hashSetOf(
                    URI("https://anidb.net/anime/8146"),
                    URI("https://anidb.net/anime/8147"),
                ),
            )

            // when
            val result = goldenRecordList.merge(uuid, otherAnime)

            // then
            assertThat(result.sources).containsExactly(
                URI("https://anidb.net/anime/4563"),
                URI("https://myanimelist.net/anime/1535"),
            )
            assertThat(result.relatedAnime).containsExactly(
                URI("https://anidb.net/anime/8146"),
                URI("https://anidb.net/anime/8147"),
                URI("https://myanimelist.net/anime/2994"),
            )
        }

        @Test
        fun `cannot merge related anime if it's the other anime's source`() {
            // given
            val goldenRecordList = DefaultGoldenRecordAccessor()

            val anime = Anime(
                _sources = hashSetOf(
                    URI("https://myanimelist.net/anime/1535"),
                ),
                _title = "Death Note",
                relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/2994"),
                ),
            )

            goldenRecordList.createGoldenRecord(anime)
            val uuid = goldenRecordList.findPossibleGoldenRecords(anime).first().id

            val otherAnime = Anime(
                _sources = hashSetOf(
                    URI("https://anidb.net/anime/4563"),
                ),
                _title = "Death Note",
                relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/1535"),
                    URI("https://anidb.net/anime/8147"),
                ),
            )

            // when
            val result = goldenRecordList.merge(uuid, otherAnime)

            // then
            assertThat(result.sources).containsExactly(
                URI("https://anidb.net/anime/4563"),
                URI("https://myanimelist.net/anime/1535"),
            )
            assertThat(result.relatedAnime).containsExactly(
                URI("https://anidb.net/anime/8147"),
                URI("https://myanimelist.net/anime/2994"),
            )
        }

        @Test
        fun `correctly updates sources cluster`() {
            // given
            val goldenRecordList = DefaultGoldenRecordAccessor()

            val anime = Anime(
                _sources = hashSetOf(
                    URI("https://myanimelist.net/anime/1535"),
                ),
                _title = "Death Note",
            )

            goldenRecordList.createGoldenRecord(anime)
            val uuid = goldenRecordList.findPossibleGoldenRecords(anime).first().id

            val otherAnime = Anime(
                _sources = hashSetOf(
                    URI("https://anidb.net/anime/4563"),
                ),
                _title = "Death Note",
            )

            // when
            goldenRecordList.merge(uuid, otherAnime)

            // then
            val result = goldenRecordList.findGoldenRecordBySource(setOf(URI("https://anidb.net/anime/4563")))
            assertThat(result).isNotNull()
        }
    }

    @Nested
    inner class ClearTests {

        @Test
        fun `clears the list`() {
            // given
            val goldenRecordList = DefaultGoldenRecordAccessor()

            val anime = Anime("Death Note")
            goldenRecordList.createGoldenRecord(anime)

            // when
            goldenRecordList.clear()

            // then
            assertThat(goldenRecordList.allEntries()).isEmpty()
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = DefaultGoldenRecordAccessor.instance

                // when
                val result = DefaultGoldenRecordAccessor.instance

                // then
                assertThat(result).isExactlyInstanceOf(DefaultGoldenRecordAccessor::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}