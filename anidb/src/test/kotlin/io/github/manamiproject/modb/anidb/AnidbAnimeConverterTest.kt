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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/title/special_chars.html")

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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/episodes/1_but_more_entries.html")

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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/episodes/1.html")

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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/episodes/10.html")

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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/episodes/100.html")

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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/episodes/1818.html")

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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/episodes/unknown.html")

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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/type/movie.html")

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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/type/music_video.html")

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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/type/other.html")

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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/type/ova.html")

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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/type/tv_series.html")

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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/type/tv_special.html")

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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/type/unknown.html")

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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/type/web.html")

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
                    loadTestResource<String>("AnidbAnimeConverterTest/picture_and_thumbnail/neither_picture_nor_thumbnail.html")

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
                    loadTestResource<String>("AnidbAnimeConverterTest/picture_and_thumbnail/picture_and_thumbnail_available.html")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.picture).isEqualTo(URI("https://cdn.anidb.net/images/main/221838.jpg"))
                assertThat(result.thumbnail).isEqualTo(URI("https://cdn.anidb.net/images/main/221838.jpg-thumb.jpg"))
            }
        }

        @Test
        fun `eu cdn is replaced by default cdn for both picture and thumbnail`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile =
                    loadTestResource<String>("AnidbAnimeConverterTest/picture_and_thumbnail/eu_cdn_replaced_by_default_cdn.html")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.picture).isEqualTo(URI("https://cdn.anidb.net/images/main/257581.jpg"))
                assertThat(result.thumbnail).isEqualTo(URI("https://cdn.anidb.net/images/main/257581.jpg-thumb.jpg"))
            }
        }

        @Test
        fun `us cdn is replaced by default cdn for both picture and thumbnail`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile =
                    loadTestResource<String>("AnidbAnimeConverterTest/picture_and_thumbnail/us_cdn_replaced_by_default_cdn.html")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.picture).isEqualTo(URI("https://cdn.anidb.net/images/main/257581.jpg"))
                assertThat(result.thumbnail).isEqualTo(URI("https://cdn.anidb.net/images/main/257581.jpg-thumb.jpg"))
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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/sources/11221.html")

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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/related_anime/no_related_anime.html")

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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/related_anime/multiple_related_anime.html")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.relatedAnime).containsExactlyInAnyOrder(
                    URI("https://anidb.net/anime/405"),
                    URI("https://anidb.net/anime/368"),
                    URI("https://anidb.net/anime/2850"),
                    URI("https://anidb.net/anime/4576"),
                    URI("https://anidb.net/anime/2995"),
                    URI("https://anidb.net/anime/6141"),
                    URI("https://anidb.net/anime/367"),
                    URI("https://anidb.net/anime/2996"),
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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/synonyms/all_types.html")

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
                    "كـتـاب الـموت",
                    "مدونة الموت",
                    "مذكرة المـوت",
                    "مذكرة الموت",
                    "موت نوٹ",
                    "डेथ नोट",
                    "デスノート",
                    "死亡笔记",
                    "데스노트",
                )
            }
        }
    }

    @Nested
    inner class StatusTests {

        @Test
        fun `FINISHED by date published`() {
            runTest {
                // given
                val fixedClock = Clock.fixed(Instant.parse("2019-11-17T15:00:00.00Z"), UTC)

                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/status/date_published.html")

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
        fun `ONGOING by date published`() {
            runTest {
                // given
                val fixedClock = Clock.fixed(Instant.parse("2013-06-13T15:00:00.00Z"), UTC)

                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/status/date_published.html")

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
        fun `UPCOMING by date published`() {
            runTest {
                // given
                val fixedClock = Clock.fixed(Instant.parse("2012-11-17T15:00:00.00Z"), UTC)

                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/status/date_published.html")

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
        fun `FINISHED by start to end`() {
            runTest {
                // given
                val fixedClock = Clock.fixed(Instant.parse("2019-11-17T15:00:00.00Z"), UTC)

                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/status/start_to_end.html")

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
        fun `ONGOING by start to end`() {
            runTest {
                // given
                val fixedClock = Clock.fixed(Instant.parse("2006-05-17T15:00:00.00Z"), UTC)

                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/status/start_to_end.html")

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
        fun `UPCOMING by start to end`() {
            runTest {
                // given
                val fixedClock = Clock.fixed(Instant.parse("2005-11-17T15:00:00.00Z"), UTC)

                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/status/start_to_end.html")

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
        fun `ONGOING by start to unknown`() {
            runTest {
                // given
                val fixedClock = Clock.fixed(Instant.parse("2023-10-17T15:00:00.00Z"), UTC)

                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/status/start_to_unknown.html")

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
        fun `UPCOMING by start to unknown`() {
            runTest {
                // given
                val fixedClock = Clock.fixed(Instant.parse("2022-10-17T15:00:00.00Z"), UTC)

                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/status/start_to_unknown.html")

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
        fun `Neither time period nor date published is mapped to UNKNOWN`() {
            runTest {
                // given
                val fixedClock = Clock.fixed(Instant.parse("2019-11-17T15:00:00.00Z"), UTC)

                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/status/unknown.html")

                val converter = AnidbAnimeConverter(
                    metaDataProviderConfig = testAnidbConfig,
                    clock = fixedClock,
                )

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(UNKNOWN_STATUS)
            }
        }
    }

    @Nested
    inner class TagsTests {

        @Test
        fun `extract multiple ignorig the link to find similar anime`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/tags/multiple_tags_with_similar.html")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.tags).containsExactlyInAnyOrder(
                    "contemporary fantasy",
                    "detective",
                    "manga",
                    "shounen",
                    "thriller",
                )
            }
        }

        @Test
        fun `empty list of no tags are available`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/tags/no_tags.html")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.tags).isEmpty()
            }
        }

        @Test
        fun `extract multiple titles if the link to search for similar anime is not present`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/tags/multiple_tags_without_similar.html")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.tags).containsExactly(
                    "new",
                    "short movie",
                )
            }
        }

        @Test
        fun `extract exactly one tag`() {
            runTest {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnidbConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/tags/one_tag.html")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.tags).containsExactly(
                    "new",
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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/duration/missing.html")

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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/duration/0_minutes.html")

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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/duration/1_minute.html")

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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/duration/25_minutes.html")

                val converter = AnidbAnimeConverter(testAnidbConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(Duration(25, MINUTES))
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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/duration/1_hour.html")

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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/duration/2_hours.html")

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
                        loadTestResource<String>("AnidbAnimeConverterTest/anime_season/year_of_premiere/2017-10-03_-_unknown.html")

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
                        loadTestResource<String>("AnidbAnimeConverterTest/anime_season/year_of_premiere/2019-07-07_-_2019-09-22.html")

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
                        loadTestResource<String>("AnidbAnimeConverterTest/anime_season/year_of_premiere/2019-08-23.html")

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

                    val testFile = loadTestResource<String>("AnidbAnimeConverterTest/anime_season/year_of_premiere/2017-10-14_-_2020.html")

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

                    val testFile = loadTestResource<String>("AnidbAnimeConverterTest/anime_season/year_of_premiere/2019-10-05_-_2020-03.html")

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

                    val testFile = loadTestResource<String>("AnidbAnimeConverterTest/anime_season/year_of_premiere/2004.html")

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

                    val testFile = loadTestResource<String>("AnidbAnimeConverterTest/anime_season/year_of_premiere/1986-06.html")

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

                    val testFile = loadTestResource<String>("AnidbAnimeConverterTest/anime_season/year_of_premiere/2020_-_unknown.html")

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

                    val testFile = loadTestResource<String>("AnidbAnimeConverterTest/anime_season/year_of_premiere/not_available.html")

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

                    val testFile = loadTestResource<String>("AnidbAnimeConverterTest/anime_season/year_of_premiere/unknown_date_of_year_-_unknown.html")

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

                    val testFile = loadTestResource<String>("AnidbAnimeConverterTest/anime_season/year_of_premiere/date_published_but_with_time_period.html")

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

            @Nested
            inner class SeasonCellTests {

                @Test
                fun `season is 'spring'`() {
                    runTest {
                        // given
                        val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                            override fun hostname(): Hostname = AnidbConfig.hostname()
                            override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                            override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                        }

                        val testFile =
                            loadTestResource<String>("AnidbAnimeConverterTest/anime_season/season/season_cell_spring.html")

                        val converter = AnidbAnimeConverter(testAnidbConfig)

                        // when
                        val result = converter.convert(testFile)

                        // then
                        assertThat(result.animeSeason.season).isEqualTo(SPRING)
                    }
                }

                @Test
                fun `season is 'summer'`() {
                    runTest {
                        // given
                        val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                            override fun hostname(): Hostname = AnidbConfig.hostname()
                            override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                            override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                        }

                        val testFile =
                            loadTestResource<String>("AnidbAnimeConverterTest/anime_season/season/season_cell_summer.html")

                        val converter = AnidbAnimeConverter(testAnidbConfig)

                        // when
                        val result = converter.convert(testFile)

                        // then
                        assertThat(result.animeSeason.season).isEqualTo(SUMMER)
                    }
                }

                @Test
                fun `season is 'fall'`() {
                    runTest {
                        // given
                        val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                            override fun hostname(): Hostname = AnidbConfig.hostname()
                            override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                            override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                        }

                        val testFile =
                            loadTestResource<String>("AnidbAnimeConverterTest/anime_season/season/season_cell_autumn.html")

                        val converter = AnidbAnimeConverter(testAnidbConfig)

                        // when
                        val result = converter.convert(testFile)

                        // then
                        assertThat(result.animeSeason.season).isEqualTo(FALL)
                    }
                }

                @Test
                fun `season is 'winter'`() {
                    runTest {
                        // given
                        val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                            override fun hostname(): Hostname = AnidbConfig.hostname()
                            override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                            override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                        }

                        val testFile =
                            loadTestResource<String>("AnidbAnimeConverterTest/anime_season/season/season_cell_winter.html")

                        val converter = AnidbAnimeConverter(testAnidbConfig)

                        // when
                        val result = converter.convert(testFile)

                        // then
                        assertThat(result.animeSeason.season).isEqualTo(WINTER)
                    }
                }
            }

            @Nested
            inner class DatePublishedCellTests {

                @ParameterizedTest
                @ValueSource(strings = ["date_published_cell_apr", "date_published_cell_may"])
                fun `season is 'spring'`(file: String) {
                    runTest {
                        // given
                        val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                            override fun hostname(): Hostname = AnidbConfig.hostname()
                            override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                            override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                        }

                        val testFile =
                            loadTestResource<String>("AnidbAnimeConverterTest/anime_season/season/$file.html")

                        val converter = AnidbAnimeConverter(testAnidbConfig)

                        // when
                        val result = converter.convert(testFile)

                        // then
                        assertThat(result.animeSeason.season).isEqualTo(SPRING)
                    }
                }

                @ParameterizedTest
                @ValueSource(strings = ["date_published_cell_jul", "date_published_cell_aug", "date_published_cell_sep"])
                fun `season is 'summer'`(file: String) {
                    runTest {
                        // given
                        val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                            override fun hostname(): Hostname = AnidbConfig.hostname()
                            override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                            override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                        }

                        val testFile =
                            loadTestResource<String>("AnidbAnimeConverterTest/anime_season/season/$file.html")

                        val converter = AnidbAnimeConverter(testAnidbConfig)

                        // when
                        val result = converter.convert(testFile)

                        // then
                        assertThat(result.animeSeason.season).isEqualTo(SUMMER)
                    }
                }

                @ParameterizedTest
                @ValueSource(strings = ["date_published_cell_oct", "date_published_cell_nov", "date_published_cell_dec"])
                fun `season is 'fall'`(file: String) {
                    runTest {
                        // given
                        val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                            override fun hostname(): Hostname = AnidbConfig.hostname()
                            override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                            override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                        }

                        val testFile = loadTestResource<String>("AnidbAnimeConverterTest/anime_season/season/$file.html")

                        val converter = AnidbAnimeConverter(testAnidbConfig)

                        // when
                        val result = converter.convert(testFile)

                        // then
                        assertThat(result.animeSeason.season).isEqualTo(FALL)
                    }
                }

                @ParameterizedTest
                @ValueSource(strings = ["date_published_cell_jan", "date_published_cell_feb", "date_published_cell_mar"])
                fun `season is 'winter'`(file: String) {
                    runTest {
                        // given
                        val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                            override fun hostname(): Hostname = AnidbConfig.hostname()
                            override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                            override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                        }

                        val testFile =
                            loadTestResource<String>("AnidbAnimeConverterTest/anime_season/season/$file.html")

                        val converter = AnidbAnimeConverter(testAnidbConfig)

                        // when
                        val result = converter.convert(testFile)

                        // then
                        assertThat(result.animeSeason.season).isEqualTo(WINTER)
                    }
                }
            }

            @Nested
            inner class StartDateCellTests {

                @ParameterizedTest
                @ValueSource(strings = ["start_date_cell_apr", "start_date_cell_may", "start_date_cell_jun"])
                fun `season is 'spring'`(file: String) {
                    runTest {
                        // given
                        val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                            override fun hostname(): Hostname = AnidbConfig.hostname()
                            override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                            override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                        }

                        val testFile =
                            loadTestResource<String>("AnidbAnimeConverterTest/anime_season/season/$file.html")

                        val converter = AnidbAnimeConverter(testAnidbConfig)

                        // when
                        val result = converter.convert(testFile)

                        // then
                        assertThat(result.animeSeason.season).isEqualTo(SPRING)
                    }
                }

                @ParameterizedTest
                @ValueSource(strings = ["start_date_cell_jul", "start_date_cell_aug", "start_date_cell_sep"])
                fun `season is 'summer'`(file: String) {
                    runTest {
                        // given
                        val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                            override fun hostname(): Hostname = AnidbConfig.hostname()
                            override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                            override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                        }

                        val testFile =
                            loadTestResource<String>("AnidbAnimeConverterTest/anime_season/season/$file.html")

                        val converter = AnidbAnimeConverter(testAnidbConfig)

                        // when
                        val result = converter.convert(testFile)

                        // then
                        assertThat(result.animeSeason.season).isEqualTo(SUMMER)
                    }
                }

                @ParameterizedTest
                @ValueSource(strings = ["start_date_cell_oct", "start_date_cell_nov", "start_date_cell_dec"])
                fun `season is 'fall'`(file: String) {
                    runTest {
                        // given
                        val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                            override fun hostname(): Hostname = AnidbConfig.hostname()
                            override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                            override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                        }

                        val testFile = loadTestResource<String>("AnidbAnimeConverterTest/anime_season/season/$file.html")

                        val converter = AnidbAnimeConverter(testAnidbConfig)

                        // when
                        val result = converter.convert(testFile)

                        // then
                        assertThat(result.animeSeason.season).isEqualTo(FALL)
                    }
                }

                @ParameterizedTest
                @ValueSource(strings = ["start_date_cell_jan", "start_date_cell_feb"])
                fun `season is 'winter'`(file: String) {
                    runTest {
                        // given
                        val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                            override fun hostname(): Hostname = AnidbConfig.hostname()
                            override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                            override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                        }

                        val testFile =
                            loadTestResource<String>("AnidbAnimeConverterTest/anime_season/season/$file.html")

                        val converter = AnidbAnimeConverter(testAnidbConfig)

                        // when
                        val result = converter.convert(testFile)

                        // then
                        assertThat(result.animeSeason.season).isEqualTo(WINTER)
                    }
                }
            }

            @Test
            fun `neither startDate nor datePublished exist therefore season is 'undefined'`() {
                runTest {
                    // given
                    val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnidbConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("AnidbAnimeConverterTest/anime_season/season/undefined.html")

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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/scores/score.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.scores).hasSize(1)
                assertThat(result.scores.first().scaledValue()).isEqualTo(8.65)
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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/scores/no-score.html")

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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/sources/11221.html")

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

                val testFile = loadTestResource<String>("AnidbAnimeConverterTest/sources/11221.html")

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