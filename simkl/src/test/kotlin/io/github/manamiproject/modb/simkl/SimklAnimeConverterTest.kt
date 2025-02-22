package io.github.manamiproject.modb.simkl

import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.anime.*
import io.github.manamiproject.modb.core.anime.AnimeRaw.Companion.NO_PICTURE
import io.github.manamiproject.modb.core.anime.AnimeRaw.Companion.NO_PICTURE_THUMBNAIL
import io.github.manamiproject.modb.core.anime.AnimeSeason.Companion.UNKNOWN_YEAR
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.anime.AnimeStatus.*
import io.github.manamiproject.modb.core.anime.AnimeType.*
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.*
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.test.loadTestResource
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.URI
import kotlin.test.Test

internal class SimklAnimeConverterTest {
    
    @Nested
    inner class TitleTests {

        @Test
        fun `correctly extracts primary title`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/title/primary_title.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.title).isEqualTo("Hagane no Renkinjutsushi")
            }
        }

        @Test
        fun `extract title from alt tag, because SimklLoginBgTitle may return other text than the title like 'Blue Box'`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/title/37171.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.title).isEqualTo("Umi no Triton")
            }
        }

        @Test
        fun `correctly extracts title including special chars`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/title/special_chars.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.title).isEqualTo("Go-Toubun no Hanayome~")
            }
        }
    }
    
    @Nested
    inner class EpisodesTests {
        
        @Test
        fun `unknown number of episodes`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/episodes/unknown.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.episodes).isEqualTo(0)
            }
        }

        @Test
        fun `1 episode`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/episodes/1.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.episodes).isOne()
            }
        }

        @Test
        fun `10 episodes`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/episodes/10.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.episodes).isEqualTo(10)
            }
        }

        @Test
        fun `100 episodes`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/episodes/100.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.episodes).isEqualTo(100)
            }
        }

        @Test
        fun `1818 episodes`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/episodes/1818.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.episodes).isEqualTo(1818)
            }
        }

        @Test
        fun `2 episodes retrieved from fallback`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/episodes/fallback.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.episodes).isEqualTo(2)
            }
        }

        @Test
        fun `correctly extracts 130 on an ongoing anime whose last episode was 107 and next is 108`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/episodes/130_episodes_while_ongoing.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.episodes).isEqualTo(130)
            }
        }

        @Test
        fun `correctly extracts episodes for a series that ended and is missing the info in the header`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/episodes/series_ended_missing_info.html")

                val converter = SimklAnimeConverter(testSimklConfig)

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
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/type/movie.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(MOVIE)
            }
        }

        @Test
        fun `type is OVA`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/type/ova.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(OVA)
            }
        }

        @Test
        fun `type is TV`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/type/tv.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(TV)
            }
        }

        @Test
        fun `type is ONA`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/type/ona.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(ONA)
            }
        }

        @Test
        fun `type is SPECIAL`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/type/special.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(SPECIAL)
            }
        }

        @Test
        fun `'Music Video' is mapped to SPECIAL`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/type/music_video.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.type).isEqualTo(SPECIAL)
            }
        }
    }

    @Nested
    inner class PictureAndThumbnailTests {

        @Test
        fun `neither picture nor thumbnail available`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/picture_and_thumbnail/neither_picture_nor_thumbnail.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.picture).isEqualTo(NO_PICTURE)
                assertThat(result.thumbnail).isEqualTo(NO_PICTURE_THUMBNAIL)
            }
        }

        @Test
        fun `picture and thumbnail`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/picture_and_thumbnail/picture_and_thumbnail_available.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.picture).isEqualTo(URI("https://simkl.in/posters/11/113489741c880d3962_m.webp"))
                assertThat(result.thumbnail).isEqualTo(URI("https://simkl.in/posters/11/113489741c880d3962_m.webp"))
            }
        }
    }

    @Nested
    inner class StatusTests {

        @Test
        fun `correctly extracts ONGOING anime`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/status/ongoing.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(ONGOING)
            }
        }

        @Test
        fun `correctly extracts UPCOMING anime`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/status/upcoming.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(UPCOMING)
            }
        }

        @Test
        fun `correctly extracts FINISHED anime`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/status/finished.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(FINISHED)
            }
        }

        @Test
        fun `airdate in the past is mapped to FINISHED`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/status/airdate-year-only-indicating-finished.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.status).isEqualTo(FINISHED)
            }
        }
    }

    @Nested
    inner class AnimeSeasonTests {

        @Nested
        inner class SeasonTests {

            @ParameterizedTest
            @ValueSource(ints = [1, 2, 3])
            fun `season is 'WINTER'`(value: Int) {
                runBlocking {
                    // given
                    val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = SimklConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("SimklAnimeConverterTest/animeSeason/season/$value.html")

                    val converter = SimklAnimeConverter(testSimklConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(WINTER)
                }
            }

            @ParameterizedTest
            @ValueSource(ints = [4, 5, 6])
            fun `season is 'SPRING'`(value: Int) {
                runBlocking {
                    // given
                    val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = SimklConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("SimklAnimeConverterTest/animeSeason/season/$value.html")

                    val converter = SimklAnimeConverter(testSimklConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(SPRING)
                }
            }

            @ParameterizedTest
            @ValueSource(ints = [7, 8, 9])
            fun `season is 'SUMMER'`(value: Int) {
                runBlocking {
                    // given
                    val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = SimklConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("SimklAnimeConverterTest/animeSeason/season/$value.html")

                    val converter = SimklAnimeConverter(testSimklConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(SUMMER)
                }
            }

            @ParameterizedTest
            @ValueSource(ints = [10, 11, 12])
            fun `season is 'FALL'`(value: Int) {
                runBlocking {
                    // given
                    val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = SimklConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("SimklAnimeConverterTest/animeSeason/season/$value.html")

                    val converter = SimklAnimeConverter(testSimklConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(FALL)
                }
            }
        }

        @Nested
        inner class YearOfPremiereTests {

            @Test
            fun `correctly extract the year of premiere if start and end year are the same`() {
                runBlocking {
                    // given
                    val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = SimklConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("SimklAnimeConverterTest/animeSeason/yearOfPremiere/same_year.html")

                    val converter = SimklAnimeConverter(testSimklConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(2023)
                }
            }

            @Test
            fun `correctly extract the year of premiere`() {
                runBlocking {
                    // given
                    val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = SimklConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("SimklAnimeConverterTest/animeSeason/yearOfPremiere/overlapping_years.html")

                    val converter = SimklAnimeConverter(testSimklConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(2009)
                }
            }

            @Test
            fun `correctly adjusts the year of premiere of it is set in year before the first official anime release`() {
                runBlocking {
                    // given
                    val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = SimklConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                        override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                    }

                    val testFile = loadTestResource<String>("SimklAnimeConverterTest/animeSeason/yearOfPremiere/release-prior-first-ever-anime.html")

                    val converter = SimklAnimeConverter(testSimklConfig)

                    // when
                    val result = converter.convert(testFile)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(YEAR_OF_THE_FIRST_ANIME)
                }
            }
        }

        @Test
        fun `both unknown`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/animeSeason/unknown.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.animeSeason.season).isEqualTo(UNDEFINED)
                assertThat(result.animeSeason.year).isEqualTo(UNKNOWN_YEAR)
            }
        }
    }

    @Nested
    inner class SourcesTests {

        @Test
        fun `correctly extracts id 41586`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/sources/41586.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.sources).containsExactlyInAnyOrder(
                    URI("https://simkl.com/anime/41586"),
                )
            }
        }
    }
    
    @Nested
    inner class TagsTests {

        @Test
        fun `correctly extracts both genres and subgenres`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/tags/genres_and_subgenres.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.tags).containsExactlyInAnyOrder(
                    "colour coded",
                    "comedy",
                    "harem",
                    "high school",
                    "japanese production",
                    "male protagonist",
                    "predominantly female cast",
                    "romance",
                    "school",
                    "school life",
                    "shounen",
                )
            }
        }
    }

    @Nested
    inner class RelatedAnimeTests {

        @Test
        fun `correctly extracts both directly and indirectly related anime`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/relatedAnime/directly_and_indirectly_related.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.relatedAnime).containsExactlyInAnyOrder(
                    URI("https://simkl.com/anime/37429"),
                    URI("https://simkl.com/anime/38317"),
                    URI("https://simkl.com/anime/39885"),
                )
            }
        }
    }

    @Nested
    inner class SynonymsTests {

        @Test
        fun `correctly extracts multiple synoynms`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/synonyms/multiple_synonyms.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    "鋼の錬金術師 FULLMETAL ALCHEMIST",
                    "Fullmetal Alchemist: Brotherhood",
                    "鋼の錬金術師",
                )
            }
        }

        @Test
        fun `no synonyms`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/synonyms/no_synonyms.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.synonyms).isEmpty()
            }
        }
    }

    @Nested
    inner class DurationTests {

        @Test
        fun `correctly extracts minutes`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/duration/minutes.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(Duration(
                    value = 26,
                    MINUTES,
                ))
            }
        }

        @Test
        fun `correctly extracts hours`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/duration/hours.html")

                val converter = SimklAnimeConverter(testSimklConfig)

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.duration).isEqualTo(Duration(
                    value = 7800,
                    SECONDS,
                ))
            }
        }
    }

    @Nested
    inner class ScoresTests {

        @Test
        fun `successfully extracts score`() {
            runBlocking {
                // given
                val testSimklConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val converter = SimklAnimeConverter(testSimklConfig)

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/scores/score.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.scores).hasSize(1)
                assertThat(result.scores.first().scaledValue()).isEqualTo(8.7)
            }
        }

        @Test
        fun `returns NoMetaDataProviderScore`() {
            runBlocking {
                // given
                val testAnidbConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = SimklConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
                }

                val converter = SimklAnimeConverter(testAnidbConfig)

                val testFile = loadTestResource<String>("SimklAnimeConverterTest/scores/no-score.html")

                // when
                val result = converter.convert(testFile)

                // then
                assertThat(result.scores).isEmpty()
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = SimklAnimeConverter.instance

            // when
            val result = SimklAnimeConverter.instance

            // then
            assertThat(result).isExactlyInstanceOf(SimklAnimeConverter::class.java)
            assertThat(result === previous).isTrue()
        }
    }
}