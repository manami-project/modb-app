package io.github.manamiproject.modb.anidb

import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.anime.AnimeStatus.*
import io.github.manamiproject.modb.core.anime.AnimeType.*
import io.github.manamiproject.modb.core.anime.Duration
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.*
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.test.loadTestResource
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.URI
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset.UTC
import kotlin.test.Test
import io.github.manamiproject.modb.core.anime.AnimeStatus.UNKNOWN as UNKNOWN_STATUS
import io.github.manamiproject.modb.core.anime.AnimeType.UNKNOWN as UNKNOWN_TYPE

internal class AnidbAnimeConverterTest {

    @Nested
    inner class TitleTests {

        @Test
        fun `title containing special chars`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/title/special_chars.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.title).isEqualTo(".hack//G.U. Trilogy")
            }
        }
    }

    @Nested
    inner class EpisodesTests {

        @Test
        fun `1 episode, although more entries are listed - the selector returns null`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/episodes/1_but_more_entries.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.episodes).isOne()
            }
        }

        @Test
        fun `1 episode -  the selector returns null`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/episodes/1.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.episodes).isOne()
            }
        }

        @Test
        fun `10 episodes`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/episodes/10.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.episodes).isEqualTo(10)
            }
        }

        @Test
        fun `100 episodes`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/episodes/100.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.episodes).isEqualTo(100)
            }
        }

        @Test
        fun `1818 episodes`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/episodes/1818.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.episodes).isEqualTo(1818)
            }
        }

        @Test
        fun `unknown number of episodes`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/episodes/unknown.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.episodes).isEqualTo(1)
            }
        }
    }

    @Nested
    inner class TypeTests {

        @Test
        fun `type is Movie`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/type/movie.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(MOVIE)
            }
        }

        @Test
        fun `type 'Music Video' is mapped to 'Special'`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/type/music_video.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(SPECIAL)
            }
        }

        @Test
        fun `type 'Other' is mapped to 'Special'`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/type/other.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(SPECIAL)
            }
        }

        @Test
        fun `type is OVA`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/type/ova.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(OVA)
            }
        }

        @Test
        fun `type 'TV Series' is mapped to 'TV'`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/type/tv_series.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(TV)
            }
        }

        @Test
        fun `type 'TV Special' is mapped to 'Special'`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/type/tv_special.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(SPECIAL)
            }
        }

        @Test
        fun `type 'Unknown' is mapped to 'UNKNOWN'`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/type/unknown.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(UNKNOWN_TYPE)
            }
        }

        @Test
        fun `type 'Web' is mapped to 'ONA'`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/type/web.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(ONA)
            }
        }
    }

    @Nested
    inner class PictureAndThumbnailTests {

        @Test
        fun `neither picture nor thumbnail`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile =
                    loadTestResource<String>("AnidbAnimeConverterTest/picture_and_thumbnail/neither_picture_nor_thumbnail.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.picture).isEqualTo(URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png"))
                assertThat(result.thumbnail).isEqualTo(URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png"))
            }
        }

        @Test
        fun `picture and thumbnail`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile =
                    loadTestResource<String>("AnidbAnimeConverterTest/picture_and_thumbnail/picture_and_thumbnail_available.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.picture).isEqualTo(URI("https://cdn.anidb.net/images/main/300398.jpg"))
                assertThat(result.thumbnail).isEqualTo(URI("https://cdn.anidb.net/images/main/300398.jpg-thumb.jpg"))
            }
        }
    }

    @Nested
    inner class SourcesTests {

        @Test
        fun `extract id 11221`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/sources/11221.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.sources).containsExactly(URI("https://anidb.net/anime/11221"))
            }
        }
    }

    @Nested
    inner class RelatedAnimeTests {

        @Test
        fun `no relations`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/related_anime/no_related_anime.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.relatedAnime).isEmpty()
            }
        }

        @Test
        fun `multiple relations`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/related_anime/multiple_related_anime.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.relatedAnime).containsExactlyInAnyOrder(
                    URI("https://anidb.net/anime/4576"),
                    URI("https://anidb.net/anime/405"),
                    URI("https://anidb.net/anime/6141"),
                    URI("https://anidb.net/anime/367"),
                    URI("https://anidb.net/anime/368"),
                    URI("https://anidb.net/anime/6393"),
                )
            }
        }
    }

    @Nested
    inner class SynonymsTests {

        @Test
        fun `gather all possible synonyms from info and titles tab`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/synonyms/all_types.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    "Bilježnica smrti",
                    "Caderno da Morte",
                    "DEATH NOTE",
                    "DN",
                    "Death Note - A halállista",
                    "Death Note - Carnetul morţii",
                    "Death Note - Zápisník smrti",
                    "Mirties Užrašai",
                    "Notatnik śmierci",
                    "Notes Śmierci",
                    "Quaderno della Morte",
                    "Sveska Smrti",
                    "Ölüm Defteri",
                    "Τετράδιο Θανάτου",
                    "Бележник на Смъртта",
                    "Записник Смерті",
                    "Свеска Смрти",
                    "Тетрадка на Смъртта",
                    "Тетрадь cмерти",
                    "Үхлийн Тэмдэглэл",
                    "מחברת המוות",
                    "دفتر الموت",
                    "دفترچه مرگ",
                    "دفترچه یادداشت مرگ",
                    "ديث نوت",
                    "مدونة الموت",
                    "مذكرة الموت",
                    "موت نوٹ",
                    "डेथ नोट",
                    "デスノート",
                    "死亡笔记",
                    "데스노트",
                    "Cuốn sổ tử thần",
                    "死亡筆記本",
                    "مفكرة الموت",
                    "สมุดโน้ตกระชากวิญญาณ",
                )
            }
        }
    }

    @Nested
    inner class StatusTests {

        @Test
        fun `FINISHED - startDate and endDate identical and in the past`() {
            runTest {
                // given
                val fixedClock = Clock.fixed(Instant.parse("2026-06-14T15:00:00.00Z"), UTC)

                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/status/finished_-_startdate_and_enddate_identical_and_in_the_past.xml")

                val converter = AnidbAnimeConverter(
                    metaDataProviderConfig = testAnidbConfig,
                    clock = fixedClock,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(FINISHED)
            }
        }

        @Test
        fun `FINISHED - startDate and endDate differing and in the past`() {
            runTest {
                // given
                val fixedClock = Clock.fixed(Instant.parse("2026-06-14T15:00:00.00Z"), UTC)

                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/status/finished_-_startdate_and_enddate_differing_and_in_the_past.xml")

                val converter = AnidbAnimeConverter(
                    metaDataProviderConfig = testAnidbConfig,
                    clock = fixedClock,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(FINISHED)
            }
        }

        @Test
        fun `FINISHED - endDate is missing but its a movie released in the past`() {
            runTest {
                // given
                val fixedClock = Clock.fixed(Instant.parse("2026-06-14T15:00:00.00Z"), UTC)

                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/status/finished_-_enddate_is_missing_but_its_a_movie_released_in_the_past.xml")

                val converter = AnidbAnimeConverter(
                    metaDataProviderConfig = testAnidbConfig,
                    clock = fixedClock,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(FINISHED)
            }
        }

        @Test
        fun `ONGOING - startDate in the past and no endDate`() {
            runTest {
                // given
                val fixedClock = Clock.fixed(Instant.parse("2026-06-14T15:00:00.00Z"), UTC)

                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/status/ongoing_-_startdate_in_the_past_and_no_enddate.xml")

                val converter = AnidbAnimeConverter(
                    metaDataProviderConfig = testAnidbConfig,
                    clock = fixedClock,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(ONGOING)
            }
        }

        @Test
        fun `ONGOING - startDate in the past and endDate in the future`() {
            runTest {
                // given
                val fixedClock = Clock.fixed(Instant.parse("2026-06-14T15:00:00.00Z"), UTC)

                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/status/ongoing_-_startdate_in_the_past_and_no_enddate.xml")

                val converter = AnidbAnimeConverter(
                    metaDataProviderConfig = testAnidbConfig,
                    clock = fixedClock,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(ONGOING)
            }
        }

        @Test
        fun `UPCOMING - both startDate and endDate in the future`() {
            runTest {
                // given
                val fixedClock = Clock.fixed(Instant.parse("2026-06-14T15:00:00.00Z"), UTC)

                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/status/upcoming_-_both_startdate_and_enddate_in_the_future.xml")

                val converter = AnidbAnimeConverter(
                    metaDataProviderConfig = testAnidbConfig,
                    clock = fixedClock,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(UPCOMING)
            }
        }

        @Test
        fun `UPCOMING - startDate in the future and no endDate`() {
            runTest {
                // given
                val fixedClock = Clock.fixed(Instant.parse("2026-06-14T15:00:00.00Z"), UTC)

                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/status/upcoming_-_startdate_in_the_future_and_no_enddate.xml")

                val converter = AnidbAnimeConverter(
                    metaDataProviderConfig = testAnidbConfig,
                    clock = fixedClock,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(UPCOMING)
            }
        }
    }

    @Nested
    inner class TagsTests {

        @Test
        fun `extract multiple tags ignoring unverified tags`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/tags/multiple_tags.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.tags).containsExactlyInAnyOrder(
                    "adapted into japanese movie",
                    "adapted into jdrama",
                    "adapted into other media",
                    "adults are useless",
                    "alternative present",
                    "americas",
                    "antihero",
                    "asia",
                    "battle of wits",
                    "bishounen",
                    "cast",
                    "contemporary fantasy",
                    "contractor",
                    "death",
                    "detective",
                    "dynamic",
                    "earth",
                    "elements",
                    "everybody dies",
                    "fantasy",
                    "fetishes",
                    "following one's dream",
                    "grail in the garbage",
                    "insane",
                    "japan",
                    "japanese production",
                    "journalism",
                    "just as planned",
                    "law and order",
                    "maintenance tags",
                    "manga",
                    "mundane made awesome",
                    "murder",
                    "mystery",
                    "origin",
                    "original work",
                    "place",
                    "plot continuity",
                    "police are useless",
                    "police",
                    "predominantly adult cast",
                    "present",
                    "real-world location",
                    "rivalry",
                    "romance",
                    "school life",
                    "secret identity",
                    "setting",
                    "shounen",
                    "speculative fiction",
                    "target audience",
                    "technical aspects",
                    "themes",
                    "thriller",
                    "time skip",
                    "time",
                    "to be moved to character",
                    "tropes",
                    "united states",
                    "university",
                    "unsorted",
                    "unusual weapons -- to be split and deleted",
                    "weekly shounen jump",
                    "world domination",
                )
            }
        }
    }

    @Nested
    inner class DurationTests {

        @Test
        fun `null results in 0 seconds`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/duration/missing.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(Duration(0, SECONDS))
            }
        }

        @Test
        fun `0 minutes - anidb does not provide seconds`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/duration/0_minutes.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(Duration(0, MINUTES))
            }
        }

        @Test
        fun `1 minute`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/duration/1_minute.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(Duration(1, MINUTES))
            }
        }

        @Test
        fun `25 minutes`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/duration/25_minutes.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(Duration(26, MINUTES))
            }
        }

        @Test
        fun `1 hour`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/duration/1_hour.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(Duration(1, HOURS))
            }
        }

        @Test
        fun `2 hours`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/duration/2_hours.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(Duration(2, HOURS))
            }
        }
    }

    @Nested
    inner class AnimeSeasonTests {

        @Nested
        inner class YearOfPremiereTests {

            @Test
            fun `2017-10-03 - unknown`() {
                runTest {
                    // given
                    val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnidbConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                    }

                    val testFile =
                        loadTestResource<String>("AnidbAnimeConverterTest/anime_season/year_of_premiere/2017-10-03_-_unknown.xml")

                    val converter = AnidbAnimeConverter(testAnidbConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(2017)
                }
            }

            @Test
            fun `2019-07-07 - 2019-09-22`() {
                runTest {
                    // given
                    val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnidbConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                    }

                    val testFile =
                        loadTestResource<String>("AnidbAnimeConverterTest/anime_season/year_of_premiere/2019-07-07_-_2019-09-22.xml")

                    val converter = AnidbAnimeConverter(testAnidbConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(2019)
                }
            }

            @Test
            fun `2019-08-23`() {
                runTest {
                    // given
                    val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnidbConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                    }

                    val testFile =
                        loadTestResource<String>("AnidbAnimeConverterTest/anime_season/year_of_premiere/2019-08-23.xml")

                    val converter = AnidbAnimeConverter(testAnidbConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(2019)
                }
            }

            @Test
            fun `2017-10-14 - 2020`() {
                runTest {
                    // given
                    val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnidbConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("AnidbAnimeConverterTest/anime_season/year_of_premiere/2017-10-14_-_2020.xml")

                    val converter = AnidbAnimeConverter(testAnidbConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(2017)
                }
            }

            @Test
            fun `2019-10-05 - 2020-03`() {
                runTest {
                    // given
                    val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnidbConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("AnidbAnimeConverterTest/anime_season/year_of_premiere/2019-10-05_-_2020-03.xml")

                    val converter = AnidbAnimeConverter(testAnidbConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(2019)
                }
            }

            @Test
            fun `2004`() {
                runTest {
                    // given
                    val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnidbConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("AnidbAnimeConverterTest/anime_season/year_of_premiere/2004.xml")

                    val converter = AnidbAnimeConverter(testAnidbConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(2004)
                }
            }

            @Test
            fun `1986-06`() {
                runTest {
                    // given
                    val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnidbConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("AnidbAnimeConverterTest/anime_season/year_of_premiere/1986-06.xml")

                    val converter = AnidbAnimeConverter(testAnidbConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(1986)
                }
            }

            @Test
            fun `2020 - unknown`() {
                runTest {
                    // given
                    val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnidbConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("AnidbAnimeConverterTest/anime_season/year_of_premiere/2020_-_unknown.xml")

                    val converter = AnidbAnimeConverter(testAnidbConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(2020)
                }
            }

            @Test
            fun `not available`() {
                runTest {
                    // given
                    val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnidbConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("AnidbAnimeConverterTest/anime_season/year_of_premiere/not_available.xml")

                    val converter = AnidbAnimeConverter(testAnidbConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(0)
                }
            }

            @Test
            fun `unknown date with year - unknown`() {
                runTest {
                    // given
                    val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnidbConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("AnidbAnimeConverterTest/anime_season/year_of_premiere/unknown_date_of_year_-_unknown.xml")

                    val converter = AnidbAnimeConverter(testAnidbConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(2021)
                }
            }

            @Test
            fun `time period but using date published`() {
                runTest {
                    // given
                    val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnidbConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("AnidbAnimeConverterTest/anime_season/year_of_premiere/date_published_but_with_time_period.xml")

                    val converter = AnidbAnimeConverter(testAnidbConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(2014)
                }
            }
        }

        @Nested
        inner class SeasonTests {

            @ParameterizedTest
            @ValueSource(strings = ["jan", "feb", "mar"])
            fun `season is 'winter'`(fileIndicator: String) {
                runTest {
                    // given
                    val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnidbConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                    }

                    val testFile =
                        loadTestResource<String>("AnidbAnimeConverterTest/anime_season/season/$fileIndicator.xml")

                    val converter = AnidbAnimeConverter(testAnidbConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(WINTER)
                }
            }

            @ParameterizedTest
            @ValueSource(strings = ["apr", "may", "jun"])
            fun `season is 'spring'`(fileIndicator: String) {
                runTest {
                    // given
                    val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnidbConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                    }

                    val testFile =
                        loadTestResource<String>("AnidbAnimeConverterTest/anime_season/season/$fileIndicator.xml")

                    val converter = AnidbAnimeConverter(testAnidbConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(SPRING)
                }
            }

            @ParameterizedTest
            @ValueSource(strings = ["jul", "aug", "sep"])
            fun `season is 'summer'`(fileIndicator: String) {
                runTest {
                    // given
                    val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnidbConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                    }

                    val testFile =
                        loadTestResource<String>("AnidbAnimeConverterTest/anime_season/season/$fileIndicator.xml")

                    val converter = AnidbAnimeConverter(testAnidbConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(SUMMER)
                }
            }

            @ParameterizedTest
            @ValueSource(strings = ["oct", "nov", "dec"])
            fun `season is 'fall'`(fileIndicator: String) {
                runTest {
                    // given
                    val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnidbConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                    }

                    val testFile =
                        loadTestResource<String>("AnidbAnimeConverterTest/anime_season/season/$fileIndicator.xml")

                    val converter = AnidbAnimeConverter(testAnidbConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(FALL)
                }
            }

            @Test
            fun `season is 'undefined', because only a year is given`() {
                runTest {
                    // given
                    val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnidbConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("AnidbAnimeConverterTest/anime_season/season/unknown_-_year_only.xml")

                    val converter = AnidbAnimeConverter(testAnidbConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(UNDEFINED)
                }
            }
        }
    }

    @Nested
    inner class ScoresTests {

        @Test
        fun `successfully extracts score`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val converter = AnidbAnimeConverter(testAnidbConfig)

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/scores/score.xml")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.scores).hasSize(1)
                assertThat(result.scores.first().scaledValue()).isEqualTo(7.61)
            }
        }

        @Test
        fun `returns NoMetaDataProviderScore`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val converter = AnidbAnimeConverter(testAnidbConfig)

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/scores/no-score.xml")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.scores).isEmpty()
            }
        }
    }

    @Nested
    inner class StudiosTests {

        @Test
        fun `always empty, because they mix up persons and actual studios`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/sources/11221.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.studios).isEmpty()
            }
        }
    }

    @Nested
    inner class ProducersTests {

        @Test
        fun `always empty, because they mix up persons and actual studios and list them under different headings`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/sources/11221.xml")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.producers).isEmpty()
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = AnidbAnimeConverter.instance

            // when
            val result = AnidbAnimeConverter.instance

            // then
            assertThat(result).isExactlyInstanceOf(AnidbAnimeConverter::class.java)
            assertThat(result === previous).isTrue()
        }
    }
}